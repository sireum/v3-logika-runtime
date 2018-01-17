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

package org.sireum

import org.scalactic.source.Position
import org.sireum.test.SireumRuntimeSpec

class MessagePackTest extends SireumRuntimeSpec {

  check(T, { w => w.writeB(T) }, { r => r.readB() })

  check(F, { w => w.writeB(F) }, { r => r.readB() })

  for (_ <- 0 until 10) {
    {
      val c: C = C.random
      check(c, { w => w.writeC(c) }, { r => r.readC() })
    }

    {
      val n: Z = Z.random
      check(n, { w => w.writeZ(n) }, { r => r.readZ() })
    }

    {
      val n: Z8 = Z8(-21)
      check(n, { w => w.writeZ8(n) }, { r => r.readZ8() })
    }

    {
      val n: Z16 = Z16.random
      check(n, { w => w.writeZ16(n) }, { r => r.readZ16() })
    }

    {
      val n: Z32 = Z32.random
      check(n, { w => w.writeZ32(n) }, { r => r.readZ32() })
    }

    {
      val n: Z64 = Z64.random
      check(n, { w => w.writeZ64(n) }, { r => r.readZ64() })
    }

    {
      val n: N = N.random
      check(n, { w => w.writeN(n) }, { r => r.readN() })
    }

    {
      val n: N8 = N8.random
      check(n, { w => w.writeN8(n) }, { r => r.readN8() })
    }

    {
      val n: N16 = N16.random
      check(n, { w => w.writeN16(n) }, { r => r.readN16() })
    }

    {
      val n: N32 = N32.random
      check(n, { w => w.writeN32(n) }, { r => r.readN32() })
    }

    {
      val n: N64 = N64.random
      check(n, { w => w.writeN64(n) }, { r => r.readN64() })
    }

    {
      val n: S8 = S8.random
      check(n, { w => w.writeS8(n) }, { r => r.readS8() })
    }

    {
      val n: S16 = S16.random
      check(n, { w => w.writeS16(n) }, { r => r.readS16() })
    }

    {
      val n: S32 = S32.random
      check(n, { w => w.writeS32(n) }, { r => r.readS32() })
    }

    {
      val n: S64 = S64.random
      check(n, { w => w.writeS64(n) }, { r => r.readS64() })
    }

    {
      val n: U8 = U8.random
      check(n, { w => w.writeU8(n) }, { r => r.readU8() })
    }

    {
      val n: U16 = U16.random
      check(n, { w => w.writeU16(n) }, { r => r.readU16() })
    }

    {
      val n: U32 = U32.random
      check(n, { w => w.writeU32(n) }, { r => r.readU32() })
    }

    {
      val n: U64 = U64.random
      check(n, { w => w.writeU64(n) }, { r => r.readU64() })
    }

    {
      val n: R = R.random
      check(n, { w => w.writeR(n) }, { r => r.readR() })
    }

    {
      val n: F32 = F32.random
      check(n, { w => w.writeF32(n) }, { r => r.readF32() })
    }

    {
      val n: F64 = F64.random
      check(n, { w => w.writeF64(n) }, { r => r.readF64() })
    }

    {
      val s: String = String.random
      check(s, { w => w.writeString(s) }, { r => r.readString() })
    }

    { val size = Z.random % 1024
      val a = (z"0" until size).map(_ => Z.random % 1024)
      check(a,
        { w =>
          w.writeArrayHeader(size)
          for (i <- z"0" until size) {
            w.writeZ(a(i))
          }
        },
        { r =>
          val size2 = r.readArrayHeader()
          val a2 = MSZ.create(size2, z"0")
          for (i <- z"0" until size2) {
            a2(i) = r.readZ()
          }
          a2.toIS
        }
      )
    }
  }

  def check[T](n: T, f: MessagePack.Writer => Unit, g: MessagePack.Reader => T)(implicit pos: Position): Unit = {
    if (B.random)
      *(s"${n.getClass.getName.substring(11)} $n") {
        val w = MessagePack.writer
        f(w)
        val r = MessagePack.reader(w.result)
        assert(g(r) == n)
        true
      }
    else
      *(s"${n.getClass.getName.substring(11)} $n") {
        val w = MessagePack.writer
        f(w)
        val r = MessagePack.readerBase64(w.resultBase64)
        assert(g(r) == n)
        true
      }
  }
}
