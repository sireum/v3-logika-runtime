// #Sireum
/*
 Copyright (c) 2017-2021, Robby, Kansas State University
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

package org.sireum.project

import org.sireum._

@sig trait ModuleProcessor[T] {
  @pure def root: Os.Path

  @pure def module: Module

  @pure def force: B

  @pure def par: B

  @pure def sha3: B

  @pure def followSymLink: B

  @pure def outDir: Os.Path

  @pure def fileFilter(file: Os.Path): B

  def process(shouldProcess: B,
              dm: DependencyManager,
              sourceFiles: ISZ[Os.Path],
              testSourceFiles: ISZ[Os.Path]): (T, B)

  def findSources(path: Os.Path): ISZ[Os.Path] = {
    return if (path.exists) for (p <- Os.Path.walk(path, F, followSymLink, fileFilter _)) yield p else ISZ()
  }

  @pure def fingerprint(p: Os.Path): String = {
    if (sha3) {
      val sha = crypto.SHA3.init256
      sha.update(p.readU8s)
      return st"${sha.finalise()}".render
    } else {
      return s"${p.lastModified}}"
    }
  }

  def run(dm: DependencyManager): T = {
    var sourceFiles = ISZ[Os.Path]()
    var testSourceFiles = ISZ[Os.Path]()
    for (source <- ProjectUtil.moduleSources(module)) {
      sourceFiles = sourceFiles ++ findSources(source)
    }
    for (testSource <- ProjectUtil.moduleTestSources(module)) {
      testSourceFiles = testSourceFiles ++ findSources(testSource)
    }

    val fingerprintMap = HashMap.empty[String, String] ++ (
      if (par && sha3) ops.ISZOps(sourceFiles ++ testSourceFiles).
        mParMap((p: Os.Path) => (root.relativize(p).string, fingerprint(p)))
      else for (p <- sourceFiles ++ testSourceFiles) yield (root.relativize(p).string, fingerprint(p))
      )

    val fingerprintCache = outDir / s"${module.id}${if (sha3) ".sha3" else ""}.json"
    val shouldProcess: B = if (!force && fingerprintCache.exists) {
      val jsonParser = Json.Parser.create(fingerprintCache.read)
      val map = jsonParser.parseHashMap(jsonParser.parseString _, jsonParser.parseString _)
      if (jsonParser.errorOpt.isEmpty) map != fingerprintMap else T
    } else {
      T
    }
    if (shouldProcess) {
      fingerprintCache.removeAll()
    }

    val (r, save) = process(shouldProcess, dm, sourceFiles, testSourceFiles)
    if (save) {
      fingerprintCache.writeOver(Json.Printer.printHashMap(F, fingerprintMap, Json.Printer.printString _,
        Json.Printer.printString _).render)
    }
    return r
  }
}

