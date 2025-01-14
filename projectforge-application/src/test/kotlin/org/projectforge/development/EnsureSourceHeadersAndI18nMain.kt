/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2022 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.development

import org.projectforge.i18n.I18nKeysUsage

fun main(args: Array<String>) {
  println("*************************************************")
  println("**** Checking and fixing source file headers. ***")
  println("*************************************************")
  SourceFileHeadersMain.main(args)
  println("*************************************************")
  println("**** Sorting and checking I18n properties.    ***")
  println("*************************************************")
  SortAndCheckI18nPropertiesMain.main(args)
  println("*************************************************")
  println("**** Analyzing and saving i18n key usage.     ***")
  println("*************************************************")
  I18nKeysUsage(I18nKeysUsage.RUN_MODE.CREATE)
}
