/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.plugins.travel

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
open class CateringDay {

    open var dayNumber: Int? = null

    // Worth 1 point
    open var breakfast: Boolean = false

    // Worth 2 points
    open var lunch: Boolean = false

    // Worth 2 points
    open var dinner: Boolean = false

    fun getNumber(): Int {
        var number = 0

        if(breakfast){
            number += 1
        }

        if(lunch){
            number += 2
        }

        if(dinner){
            number += 2
        }

        return number
    }
}
