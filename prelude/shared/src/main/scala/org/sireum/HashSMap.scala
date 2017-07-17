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

package org.sireum

object HashSMap {

  @pure def empty[K, V]: HashSMap[K, V] = {
    return HashSMap(HashMap.empty, ISZ())
  }

  @pure def emptyInit[K, V](initialCapacity: Z): HashSMap[K, V] = {
    return HashSMap(HashMap.emptyInit(initialCapacity), ISZ())
  }

}

@datatype class HashSMap[K, V](map: HashMap[K, V],
                               keys: ISZ[K]) {
  @pure def size: Z = {
    return keys.size
  }

  @pure def hash: Z = {
    return map.hash
  }

  @pure def values: ISZ[V] = {
    return map.values
  }

  @pure def keySet: Set[K] = {
    return Set.empty[K].addAll(keys)
  }

  @pure def valueSet: Set[V] = {
    return Set.empty[V].addAll(values)
  }

  @pure def put(key: K, value: V): HashSMap[K, V] = {
    val newMap = map.put(key, value)
    if (newMap.size == size) {
      return this
    } else {
      return HashSMap(newMap, keys :+ key)
    }
  }

  @pure def get(key: K): Option[V] = {
    return map.get(key)
  }

  @pure def removeAll(keys: ISZ[K]): HashSMap[K, V] = {
    return HashSMap(map.removeAll(keys), this.keys -- keys)
  }

  @pure def remove(key: K, value: V): HashSMap[K, V] = {
    return HashSMap(map.remove(key, value), keys - key)
  }

  @pure def isEqual(other: HashSMap[K, V]): B = {
    return map.isEqual(other.map)
  }

  @pure def contains(key: K): B = {
    return map.contains(key)
  }

  @pure def isEmpty: B = {
    return size == z"0"
  }

  @pure def nonEmpty: B = {
    return size != z"0"
  }

}