/*
 Copyright (c) 2017, Robby, Kansas State University
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.logika

import scala.meta._
import scala.meta.dialects.Scala212

class record extends scala.annotation.StaticAnnotation {
  inline def apply(tree: Any): Any = meta {
    val result: Stat = tree match {
      case r@q"..$mods trait $tname[..$tparams] extends { ..$estats } with ..$ctorcalls { $param => ..$_ }" =>
        if (mods.size != 1 || !mods.head.isInstanceOf[Mod.Sealed] ||
          estats.nonEmpty || ctorcalls.nonEmpty || !param.name.isInstanceOf[Name.Anonymous])
          abort(s"Invalid Logika @record form on a trait; it has to be of the form 'sealed trait ${tname.value} { ... }'.")
        r
      case q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends { ..$estats } with ..$ctorcalls { $param => ..$stats }" =>
        if (mods.nonEmpty || ctorMods.nonEmpty || paramss.size > 1 ||
          estats.nonEmpty || ctorcalls.size > 1 || !param.name.isInstanceOf[Name.Anonymous])
          abort(s"Invalid Logika @record form on a class; it has to be of the form 'class ${tname.value}(...) { ... }'.")
        val tVars = tparams.map { tp =>
          val tparam"..$mods $tparamname[..$_] >: $_ <: $_ <% ..$_ : ..$_" = tp
          Type.Name(tparamname.value)
        }
        val tpe = {
          if (tVars.isEmpty) tname else t"$tname[..$tVars]"
        }
        val ctorName = Ctor.Name(tname.value)
        if (paramss.nonEmpty) {
          var varNames: Vector[Term.Name] = Vector()
          var cparams: Vector[Term.Param] = Vector()
          var applyParams: Vector[Term.Param] = Vector()
          var oApplyParams: Vector[Term.Param] = Vector()
          var applyArgs: Vector[Term.Name] = Vector()
          var visibleArgs: Vector[Term.Name] = Vector()
          var vars: Vector[Stat] = Vector()
          for (param <- paramss.head) param match {
            case param"..$mods $paramname: $atpeopt = $expropt" if (atpeopt match {
              case Some(targ"${tpe: Type}") => true
              case _ => false
            }) =>
              val varName = Term.Name("_" + paramname.value)
              val paramName = Term.Name(paramname.value)
              var hidden = false
              var isVar = false
              mods.foreach {
                case mod"@hidden" => hidden = true
                case mod"varparam" => isVar = true
                case _ => false
              }
              varNames :+= varName
              cparams :+= param"private var $varName: $atpeopt"
              vars :+= q"def $paramName = $varName"
              vars :+= q"def ${Term.Name(paramname.value + "_=")}($paramname: $atpeopt): this.type = { dirty = true; $varName = $paramName; this }"
              applyParams :+= param"$paramname: $atpeopt = this.$varName"
              oApplyParams :+= param"$paramname: $atpeopt"
              applyArgs :+= paramName
              if (!hidden) {
                val Some(targ"${tpe: Type}") = atpeopt
                visibleArgs :+= varName
              }
            case _ => abort(param.pos, "Unsupported Logika @datatype parameter form.")
          }
          val cls = {
            val clone = q"override def clone: $tpe = new $ctorName(..${applyArgs.map(arg => q"org.sireum.logika._clone($arg)")})"
            val hashCodeDirty = q"private var dirty: Boolean = true"
            val hashCodeVar = q"private var _hashCode: Int = _"
            val hashCodeDef = q"private def computeHashCode: Int = (classOf[$tname], ..$visibleArgs).hashCode"
            val hashCode = q"override def hashCode: Int = { if (dirty) { dirty = false; _hashCode = computeHashCode}; _hashCode }"
            val equals = {
              val eCaseEqs = visibleArgs.map(arg => q"$arg == o.$arg")
              val eCaseExp = eCaseEqs.tail.foldLeft(eCaseEqs.head)((t1, t2) => q"$t1 && $t2")
              val eCases =
                Vector(if (tparams.isEmpty) p"case o: $tname => $eCaseExp"
                else p"case (o: $tname[..$tVars] @unchecked) => $eCaseExp",
                  p"case _ => false")
              q"override def equals(o: Any): Boolean = if (this eq o.asInstanceOf[AnyRef]) true else o match { ..case $eCases }"
            }
            val apply = q"def apply(..$applyParams): $tpe = new $ctorName(..$applyArgs)"
            val toString = {
              var appends = applyArgs.map(arg => q"org.sireum.logika._append(sb, $arg)")
              appends =
                if (appends.isEmpty) appends
                else appends.head +: appends.tail.flatMap(a => Vector(q"""sb.append(", ")""", a))
              q"""override def toString(): java.lang.String = {
                    val sb = new StringBuilder
                    sb.append(${tname.value})
                    sb.append('(')
                    ..$appends
                    sb.append(')')
                    sb.toString
                  }"""
            }
            q"class $tname[..$tparams](...${Vector(cparams)}) extends {} with org.sireum.logika._Immutable with org.sireum.logika._Clonable with ..$ctorcalls { ..${(hashCodeDirty +: vars) ++ Vector(hashCodeVar, hashCodeDef, hashCode, equals, clone, apply, toString) ++ stats} }"
          }
          val companion = {
            val apply =
              if (tparams.isEmpty)
                q"def apply(..$oApplyParams): $tpe = new $ctorName(..$applyArgs)"
              else
                q"def apply[..$tparams](..$oApplyParams): $tpe = new $ctorName(..$applyArgs)"
            q"object ${Term.Name(tname.value)} { $apply }"
          }
          Term.Block(Vector(cls, companion))
        } else {
          val cls = {
            val clone = q"override def clone: $tpe = new $ctorName()"
            val hashCode = q"override val hashCode: Int = classOf[$tname].hashCode"
            val equals = {
              val eCases =
                Vector(if (tparams.isEmpty) p"case o: $tname => true"
                else p"case o: $tname[..$tVars] => true",
                  p"case _ => false")
              q"override def equals(o: Any): Boolean = if (this eq o.asInstanceOf[AnyRef]) true else o match { ..case $eCases }"
            }
            val toString = {
              val r = tname.value + "()"
              q"""override def toString(): java.lang.String = $r"""
            }
            q"class $tname[..$tparams](...$paramss) extends {} with org.sireum.logika._Immutable with org.sireum.logika._Clonable with ..$ctorcalls { ..${Vector(hashCode, equals, clone, toString) ++ stats} }"
          }
          val companion = {
            val apply =
              if (tparams.isEmpty)
                q"def apply(): $tpe = new $ctorName()"
              else
                q"def apply[..$tparams](): $tpe = new $ctorName()"
            q"object ${Term.Name(tname.value)} { $apply }"
          }
          Term.Block(Vector(cls, companion))
        }
      case Term.Block(Seq(cls, _: Defn.Object)) =>
        abort(s"Cannot use Logika @record on a class with an existing companion object.")
      case _ =>
        abort(s"Invalid Logika @record on: ${tree.syntax}.")
    }
    //println(result.syntax)
    result
  }
}
