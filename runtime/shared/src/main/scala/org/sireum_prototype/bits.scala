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

package org.sireum_prototype

import scala.meta._

object bits {
  def q(signed: Boolean, width: Int, index: Boolean, name: String): Term.Block = {
    val (min, max) =
      if (signed) (Lit.Long((BigInt(-2).pow(width - 1) + 1).toLong), Lit.Long((BigInt(2).pow(width - 1) - 1).toLong))
      else (Lit.Long(0l), Lit.Long((BigInt(2).pow(width) - 1).toLong))
    val typeName = Type.Name(name)
    val termName = Term.Name(name)
    val ctorName = Ctor.Name(name)
    Term.Block(List(
      q"""final class $typeName(val value: scala.Long) extends AnyVal with Z.BV.Long[$typeName] {
                @inline def BitWidth: scala.Int = ${Lit.Int(width)}
                @inline def Min: Z = $min
                @inline def Max: Z = $max
                @inline def isIndex: scala.Boolean = ${Lit.Boolean(index)}
                @inline def isSigned: scala.Boolean = ${Lit.Boolean(signed)}
                def make(v: scala.Long): $typeName = {
                  assert(Min <= v && v <= Max)
                  $termName(v)
                }
              }""",
      q"""object $termName {
                val Min: Z = $min
                val Max: Z = $max
                def apply(value: scala.Long): $typeName = {
                  assert(Min <= value && value <= Max)
                  new $ctorName(value)
                }
                def apply(value: Predef.String): $typeName = {
                  val v = scala.BigInt(value)
                  assert(Min <= v && v <= Max)
                  new $ctorName(v.toLong)
                }
              }"""
    ))
  }
}

class bits(signed: Boolean, width: Int) extends scala.annotation.StaticAnnotation {
  inline def apply(tree: Any): Any = meta {
    tree match {
      case q"class $tname" =>
        val q"new bits(..$args)" = this
        var width: Int = 0
        var signed: Boolean = false
        for (arg <- args) {
          arg match {
            case arg"signed = ${Term.Name("F")}" => signed = false
            case arg"signed = ${Term.Name("T")}" => signed = true
            case arg"signed = ${Lit.Boolean(b)}" => signed = b
            case arg"width = ${Lit.Int(n)}" =>
              n match {
                case 8 | 16 | 32 | 64 =>
                case _ => abort(arg.pos, s"Invalid Slang @bits width argument: ${arg.syntax} (only 8, 16, 32, or 64 are currently supported)")
              }
              width = n
            case _ => abort(arg.pos, s"Invalid Slang @bits argument: ${arg.syntax}")
          }
        }
        val result = bits.q(signed, width, index = false, tname.value)
        //println(result)
        result
      case _ => abort(tree.pos, s"Invalid Slang @bits on: ${tree.structure}")
    }
  }
}
