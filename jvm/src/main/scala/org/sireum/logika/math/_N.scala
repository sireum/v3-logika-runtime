/*
 * Copyright (c) 2016, Robby, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.logika.math

import org.apfloat.Apint
import org.sireum.logika.Z
import scala.math.ScalaNumericConversions

import org.sireum.logika.{B, N}

object _N extends LogikaNumberCompanion {

  final val zero: N = NImpl(_Z.zero)
  final val one: N = NImpl(_Z.one)

  private[logika] def checkRange(n: Z): N = {
    require(n >= 0)
    apply(n)
  }

  @inline
  final def apply(n: Int): N = apply(_Z(n))

  @inline
  final def apply(n: Long): N = apply(_Z(n))

  @inline
  final def apply(n: String): N = apply(_Z(n))

  @inline
  final def apply(n: BigInt): N = apply(_Z(n))

  @inline
  final def apply(n: java.math.BigInteger): N = apply(_Z(n))

  @inline
  final def apply(n: Apint): N = apply(_Z(n))

  @inline
  final def apply(z: _Z): N = if (z <= 0) zero else NImpl(z)

  final override def random: N = {
    val z = _Z.random
    if (z < _Z.zero) _N(-z) else _N(z)
  }
}

sealed trait _N extends ScalaNumericConversions with Comparable[_N] with LogikaIntegralNumber {
  final def +(other: N): N = _N(toZ + other.toZ)

  final def -(other: N): N = _N(toZ - other.toZ)

  final def *(other: N): N = _N(toZ * other.toZ)

  final def /(other: N): N = _N(toZ / other.toZ)

  final def %(other: N): N = _N(toZ % other.toZ)

  final def >(other: N): B = toZ > other.toZ

  final def >=(other: N): B = toZ >= other.toZ

  final def <(other: N): B = toZ < other.toZ

  final def <=(other: N): B = toZ <= other.toZ

  final override def toBigInteger: java.math.BigInteger = toZ.toBigInteger

  final override def toBigInt: BigInt = toZ.toBigInt

  final def toApint: Apint = toZ.toApint

  final override def doubleValue: Double = toBigInt.doubleValue

  final override def floatValue: Float = toBigInt.floatValue

  final override def intValue: Int = toBigInt.intValue

  final override def longValue: Long = toBigInt.longValue

  final override def underlying: java.math.BigInteger = toBigInteger

  final override def isWhole = true

  final override def toString: String = toZ.toString
}

final private case class NImpl(value: _Z) extends _N {
  override def toZ: _Z = value

  override lazy val hashCode: Int = value.hashCode

  override def equals(other: Any): Boolean = other match {
    case other: LogikaIntegralNumber => (this eq other) || value.equals(other.toZ)
    case other: Byte => value == _Z(other)
    case other: Char => value == _Z(other)
    case other: Short => value == _Z(other)
    case other: Int => value == _Z(other)
    case other: Long => value == _Z(other)
    case other: java.math.BigInteger => value == _Z(other)
    case other: BigInt => value == _Z(other)
    case _ => false
  }

  override def compareTo(other: _N): Int = other match {
    case other: NImpl => value.compareTo(other.value)
  }
}
