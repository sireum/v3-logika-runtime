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
  def q(signed: Boolean, width: Int, min: Long, max: Long, index: Long, name: String): Term.Block = {
    val typeName = Type.Name(name)
    val termName = Term.Name(name)
    val lowerTermName = Term.Name(name.toLowerCase)
    val ctorName = Ctor.Name(name)
    val nameStr = Lit.String(name)
    Term.Block(List(
      q"""final class $typeName(val value: scala.Long) extends AnyVal with Z.BV.Long[$typeName] {
            @inline def Name: Predef.String = $termName.Name
            @inline def BitWidth: scala.Int = $termName.BitWidth
            @inline def Min: $typeName = $termName.Min
            @inline def Max: $typeName = $termName.Max
            @inline def Index: $typeName = $termName.Index
            @inline def isSigned: scala.Boolean = $termName.isSigned
            def make(v: scala.Long): $typeName = $termName(v)
          }""",
      q"""object $termName {
            val Name: Predef.String = $nameStr
            val BitWidth: scala.Int = ${Lit.Int(width)}
            val Min: $typeName = new $ctorName(${Lit.Long(min)})
            val Max: $typeName = new $ctorName(${Lit.Long(max)})
            val Index: $typeName = new $ctorName(${Lit.Long(index)})
            val isSigned: scala.Boolean = ${Lit.Boolean(signed)}
            def apply(value: Z): $typeName = value match {
              case value: Z.MP => value
              case _ => halt(s"Unsupported $$Name creation from $${value.Name}.")
            }
            def unapply(n: $typeName): scala.Option[Z] = scala.Some(Z.MP(n.toBigInt))
            object Int {
              def apply(n: scala.Int): $typeName = new $ctorName(n)
              def unapply(n: $typeName): scala.Option[scala.Int] = scala.Some(v.toBigInt.toInt)
            }
            object Long {
              def apply(n: scala.Long): $typeName = new $ctorName(n)
              def unapply(n: $typeName): scala.Option[scala.Long] = scala.Some(v.value)
            }
            object String {
              def apply(s: Predef.String): $typeName = BigInt(scala.BigInt(s))
              def unapply(n: $typeName): scala.Option[Predef.String] = scala.Some(n.toBigInt.toString)
            }
            object BigInt {
              def apply(n: scala.BigInt): $typeName = if (isSigned) BitWidth match {
                case 8 => new $ctorName(n.toByte)
                case 16 => new $ctorName(n.toShort)
                case 32 => new $ctorName(n.toInt)
                case 64 => new $ctorName(n.toLong)
              } else BitWidth match {
                case 8 => new $ctorName(spire.math.UByte(n.toByte).toLong)
                case 16 => new $ctorName(spire.math.UShort(n.toShort).toLong)
                case 32 => new $ctorName(spire.math.UShort(n.toInt).toLong)
                case 64 => new $ctorName(n.toLong)
              }
              def unapply(n: $typeName): scala.Option[scala.BigInt] = scala.Some(n.toBigInt)
            }
            implicit class $$Slang(val sc: StringContext) {
              object $lowerTermName {
                def apply(args: Any*): $typeName = {
                  assume(args.isEmpty && sc.parts.length == 1)
                  String(sc.parts.head)
                }
                def unapply(n: $typeName): scala.Boolean = {
                  assume(sc.parts.length == 1)
                  n == String(sc.parts.head)
                }
              }
            }
            import scala.language.implicitConversions
            def apply(value: Z.MP): $typeName = BigInt(value.toBigInt)
          }"""
    ))
  }
}

class bits(signed: Boolean,
           width: Int,
           min: Option[BigInt] = None,
           max: Option[BigInt] = None,
           index: Option[BigInt] = None) extends scala.annotation.StaticAnnotation {
  inline def apply(tree: Any): Any = meta {
    tree match {
      case q"class $tname" =>
        val q"new bits(..$args)" = this
        var width: Int = 0
        var signed: Boolean = false
        var index: BigInt = 0
        var minOpt: Option[BigInt] = None
        var maxOpt: Option[BigInt] = None
        for (arg <- args) {
          arg match {
            case arg"signed = ${Term.Name("F")}" => signed = false
            case arg"signed = ${Term.Name("T")}" => signed = true
            case arg"signed = ${Lit.Boolean(b)}" => signed = b
            case arg"min = ${exp: Term}" => minOpt = helper.extractInt(exp)
            case arg"max = ${exp: Term}" => maxOpt = helper.extractInt(exp)
            case arg"index = ${exp: Term}" =>
              helper.extractInt(exp) match {
                case Some(n) => index = n
                case _ =>
              }
            case arg"width = ${Lit.Int(n)}" =>
              n match {
                case 8 | 16 | 32 | 64 =>
                case _ => abort(arg.pos, s"Invalid Slang @bits width argument: ${arg.syntax} (only 8, 16, 32, or 64 are currently supported)")
              }
              width = n
            case _ => abort(arg.pos, s"Invalid Slang @bits argument: ${arg.syntax}")
          }
        }
        val (wMin, wMax) =
          if (signed) (BigInt(-2).pow(width - 1) + 1, BigInt(2).pow(width - 1) - 1)
          else (BigInt(0), BigInt(2).pow(width) - 1)
        val min = minOpt.getOrElse(wMin)
        val max = maxOpt.getOrElse(wMax)
        if (min > max) abort(tree.pos, s"Slang @bits ${tname.value}'s min ($min) should not be greater than its max ($max).")
        val signedString = if (signed) "signed" else "unsigned"
        if (min < wMin) abort(tree.pos, s"Slang @bits ${tname.value}'s min ($min) should not be less than its $signedString bit-width minimum ($wMin).")
        if (max > wMax) abort(tree.pos, s"Slang @bits ${tname.value}'s max ($max) should not be greater than its $signedString bit-width maximum ($wMax).")
        if (index < min) abort(tree.pos, s"Slang @bits ${tname.value}'s index ($index) should not be less than its min ($min).")
        if (index > max) abort(tree.pos, s"Slang @bits ${tname.value}'s index ($index) should not be less than its max ($max).")
        val result = bits.q(signed, width, min.toLong, max.toLong, index.toLong, tname.value)
        //println(result)
        result
      case _ => abort(tree.pos, s"Invalid Slang @bits on: ${tree.structure}")
    }
  }
}
