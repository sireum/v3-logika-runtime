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

package org.sireumproto

import org.sireum.test.SireumRuntimeSpec
import spire.math._

import scala.util.{Failure, Success, Try}

class ZTest extends SireumRuntimeSpec {

  val numOfRandomTests = 64

  val x: Z = Z.random

  *(x.toIndex == x)

  *(!x.isBitVector)

  *(!x.hasMin)

  *(!x.hasMax)

  *(x.isSigned)

  *(x.Index == z"0")

  *(x.Name == "Z")

  *(x.decrease.increase == x)

  val random = new java.util.Random

  def rand(): scala.BigInt = Z.random.toBigInt

  for ((op, op1, op2) <- List[(Predef.String, Z => Z => Z, scala.BigInt => scala.BigInt => scala.BigInt)](
    ("+", _.+, _.+), ("-", _.-, _.-), ("*", _.*, _.*), ("/", _./, _./), ("%", _.%, _.%))) {
    for (_ <- 0 until numOfRandomTests) {
      val n = rand()
      var m = rand()
      while (m == 0 && (op == "/" || op == "%")) m = rand()
      *(s"$n $op $m")(op1(Z(n))(Z(m)).toBigInt == op2(n)(m))
    }
  }

  for ((op, op1, op2) <- List[(Predef.String, Z => Z => B, scala.BigInt => scala.BigInt => scala.Boolean)](
    (">", _.>, _.>), (">=", _.>=, _.>=), ("<", _.<, _.<), ("<=", _.<=, _.<=), ("==", _.==, _.==), ("!=", _.!=, _.!=))) {
    for (_ <- 0 until numOfRandomTests) {
      val n = rand()
      val m = rand()
      *(s"$n $op $m")(op1(Z(n))(Z(m)).value == op2(n)(m))
    }
  }
}