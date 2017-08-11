// #Sireum
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


@ext trait Nothing


@ext trait Immutable {

  @pure def string: String

}


@ext trait Equal[E <: Equal[E]] extends Immutable {

  @pure def isEqual(other: E): B

  @pure def hash: Z

}


@ext trait Ordered[O <: Ordered[O]] extends Equal[O] {

  @pure def <(other: O): B

  @pure def <=(other: O): B

  @pure def >(other: O): B

  @pure def >=(other: O): B

}


@ext trait Number[N <: Number[N]] extends Ordered[N] {

  @pure def +(other: N): N

  @pure def -(other: N): N

  @pure def *(other: N): N

  @pure def /(other: N): N

  @pure def %(other: N): N
}


@ext trait B extends Equal[B] {

  @pure def &(other: B): B

  @pure def |(other: B): B

  @pure def |^(other: B): B

  @pure def &&(other: => B): B

  @pure def ||(other: => B): B

  @pure def unary_!(): B

  @pure def unary_~(): B

}


@ext trait C extends Ordered[C]


@ext trait Z extends Number[Z] {

  @pure def BitWidthOpt: Option[Z]

  @pure def MinOpt: Option[Z]

  @pure def MaxOpt: Option[Z]

  @pure def Min: Z

  @pure def Max: Z

  @pure def BitWidth: Z

  @pure def >>(other: Z): Z

  @pure def >>>(other: Z): Z

  @pure def <<(other: Z): Z

  @pure def &(other: Z): Z

  @pure def |(other: Z): Z

  @pure def |^(other: Z): Z

  @pure def unary_~(): B

  @pure def increase: Z

  @pure def decrease: Z
}


@ext trait FloatingPoint extends Number[FloatingPoint] {

  @pure def BitWidth: Z

  @pure def SignificandBitWidth: Z

  @pure def ExponentBitWidth: Z

}


@ext trait R extends Number[R]


@ext trait String extends Equal[String]


@ext trait IS[I <: Integral[I], V <: Immutable] extends Equal[IS[I, V]] {

  @pure def size: Z

}


@ext trait Datatype[O <: Datatype[O]] extends Equal[O]


@ext trait Sig extends Immutable


@ext trait Rich extends Immutable


@ext trait ST extends Immutable



@ext trait Mutable {

  @pure def string: String

}


@ext trait MEqual[E <: MEqual[E]] extends Mutable {

  @pure def isEqual(other: E): B

  @pure def hash: Z

}


@ext trait MOrdered[O <: MOrdered[O]] extends MEqual[O] {

  @pure def <(other: O): B

  @pure def <=(other: O): B

  @pure def >(other: O): B

  @pure def >=(other: O): B

}


@ext trait MS[I <: Integral[I], V] extends MEqual[MS[I, V]] {
  @pure def size: Z
}


@ext trait Record[O <: Record[O]] extends MEqual[O]


@ext trait MSig extends Mutable