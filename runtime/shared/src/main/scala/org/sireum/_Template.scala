/*
 * Copyright (c) 2017, Robby, Kansas State University
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

package org.sireum

import java.util.StringTokenizer

object _Template {

  sealed trait Arg

  final case class Any(args: scala.Seq[scala.Any], sep: Predef.String = "") extends Arg

  final case class Templ(args: scala.Seq[ST], sep: Predef.String = "") extends Arg

  private def clean(s: Predef.String): Predef.String = s.replaceAllLiterally("\r", "")

  def render(t: ST): String = {
    val lineSep = System.lineSeparator
    val sb = new StringBuilder
    var indent = 0

    def appendPart(s: Predef.String): Unit = {
      val tkns = new StringTokenizer(clean(s), "\n", true)
      var n = 0
      var hasLine = false
      while (tkns.hasMoreTokens) {
        val tkn = tkns.nextToken()
        if (tkn == "\n") {
          sb.append(lineSep)
          appendIndent()
          hasLine = true
        } else {
          val stripped = if (hasLine) {
            val i = tkn.indexOf("|")
            if (i >= 0 && tkn.substring(0, i).forall(_.isWhitespace)) tkn.substring(i + 1) else tkn
          } else tkn
          sb.append(stripped)
          n = stripped.size
        }
      }
      if (hasLine) {
        indent += n
      }
    }

    def appendIndent(): Unit = for (i <- 0 until indent) sb.append(' ')

    def append(s: Predef.String): Unit = {
      val tkns = new StringTokenizer(clean(s), "\n", true)
      while (tkns.hasMoreTokens) {
        val tkn = tkns.nextToken()
        if (tkn == "\n") {
          sb.append(lineSep)
          appendIndent()
        } else {
          sb.append(tkn)
        }
      }
    }

    def rec(t: ST): Unit = {
      val oldIndent = indent
      val parts = t.parts
      appendPart(parts.head)
      var i = 1
      for (arg <- t.args) {
        arg match {
          case Any(vs, sep) =>
            if (vs.nonEmpty) {
              append(vs.head.toString)
              for (i <- 1 until vs.length) {
                append(sep)
                append(vs(i).toString)
              }
            }
          case Templ(ts, sep) =>
            if (ts.nonEmpty) {
              rec(ts.head)
              for (i <- 1 until ts.length) {
                append(sep)
                rec(ts(i))
              }
            }
        }
        indent = oldIndent
        appendPart(parts(i))
        i += 1
      }
    }

    rec(t)
    _2String(sb.toString)
  }

}

import _Template._

final case class _Template(parts: scala.Seq[Predef.String],
                           args: scala.Seq[Arg],
                           source: Predef.String) extends _Immutable {
  def hash: Z = _Z(hashCode)

  def render: String = _Template.render(this)

  override def toString(): java.lang.String = source
}