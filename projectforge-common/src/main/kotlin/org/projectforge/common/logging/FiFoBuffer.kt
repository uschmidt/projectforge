/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2021 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.common.logging

class FiFoBuffer<T>(private val maxSize: Int) {
  private val list = mutableListOf<T>()
  fun add(element: T) {
    synchronized(list) {
      if (list.size >= maxSize) {
        val it = list.iterator()
        it.next()
        it.remove()
      }
      list.add(element)
    }
  }

  operator fun get(index: Int): T? {
    synchronized(list) {
      return if (index <= 0 || index >= list.size) {
        null
      } else list[index]
    }
  }

  val size: Int
    get() = list.size
}