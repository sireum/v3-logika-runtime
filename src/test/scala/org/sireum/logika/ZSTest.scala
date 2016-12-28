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
package org.sireum.logika

import org.sireum.logika.test.LogikaSpec
import scala.collection.mutable.ArrayBuffer

class ZSTest extends LogikaSpec {
  final val size = math.Z(1024)
  final val zs123 = new collection.ZS.ValueArray(ArrayBuffer[Z](1, 2, 3))
  final val zs12 = new collection.ZS.ValueArray(ArrayBuffer[Z](1, 2))
  final val zs23 = new collection.ZS.ValueArray(ArrayBuffer[Z](2, 3))

  "append" - {
    * (zs123 == zs12 :+ 3)
    * (zs123.upgrade == zs12.upgrade :+ 3)
    * (zs123.upgrade == zs12 :+ 3)
    * (zs123 == zs12.upgrade :+ 3)
    * (zs123.hashCode == (zs12 :+ 3).hashCode)
    * (zs123.upgrade.hashCode == (zs12.upgrade :+ 3).hashCode)
    * (zs123.upgrade.hashCode == (zs12 :+ 3).hashCode)
    * (zs123.hashCode == (zs12.upgrade :+ 3).hashCode)
  }

  "prepend" - {
    * (zs123 == 1 +: zs23)
    * (zs123.upgrade == 1 +: zs23.upgrade)
    * (zs123 == 1 +: zs23.upgrade)
    * (zs123.upgrade == 1 +: zs23)
    * (zs123.hashCode == (1 +: zs23).hashCode)
    * (zs123.upgrade.hashCode == (1 +: zs23.upgrade).hashCode)
    * (zs123.hashCode == (1 +: zs23.upgrade).hashCode)
    * (zs123.upgrade.hashCode == (1 +: zs23).hashCode)
  }

  "impl" - {

    "zsArray" in {
      var i = math.Z.zero
      var append: ZS = new collection.ZS.ValueArray(ArrayBuffer())
      var prepend: ZS = new collection.ZS.ValueArray(ArrayBuffer())
      while (i < size) {
        append :+= i
        prepend +:= size - i - 1
        i += 1
      }
      assert(append == prepend)
    }

    "zsImpl" in {
      var i = math.Z.zero
      var append: ZS = new collection.ZS.ValueTreeMap(new java.util.TreeMap[Z, Z], 0)
      var prepend: ZS = new collection.ZS.ValueTreeMap(new java.util.TreeMap[Z, Z], 0)
      while (i < size) {
        append :+= i
        prepend +:= size - i - 1
        i += 1
      }
      assert(append == prepend)
    }

    "zsArrayImpl" in {
      var i = math.Z.zero
      var append: ZS = new collection.ZS.ValueTreeMap(new java.util.TreeMap[Z, Z], 0)
      var prepend: ZS = new collection.ZS.ValueArray(ArrayBuffer())
      while (i < size) {
        append :+= i
        prepend +:= size - i - 1
        i += 1
      }
      assert(append == prepend)
    }

    "zsImplArray" in {
      var i = math.Z.zero
      var append: ZS = new collection.ZS.ValueTreeMap(new java.util.TreeMap[Z, Z], 0)
      var prepend: ZS = new collection.ZS.ValueArray(ArrayBuffer())
      while (i < size) {
        append :+= i
        prepend +:= size - i - 1
        i += 1
      }
      assert(append == prepend)
    }
  }
}