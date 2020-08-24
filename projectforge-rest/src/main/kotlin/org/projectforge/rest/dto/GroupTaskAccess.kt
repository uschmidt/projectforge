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

package org.projectforge.rest.dto

import org.projectforge.framework.access.AccessEntryDO
import org.projectforge.framework.access.AccessType
import org.projectforge.framework.access.GroupTaskAccessDO

class GroupTaskAccess(
        var group: Group? = Group(),
        var task: Task? = Task(),
        var isRecursive: Boolean = true,
        var description: String? = null,
        var accessEntries: MutableSet<AccessEntryDO>? = mutableSetOf(),
        var formattedAccessEntries: String? = null
): BaseDTO<GroupTaskAccessDO>() {

    override fun copyFrom(src: GroupTaskAccessDO) {
        super.copyFrom(src)

        if(src.group != null){
            group!!.copyFrom(src.group!!)
        }

        if(src.task != null){
            task!!.copyFrom(src.task!!)
        }

        if(src.accessEntries != null){
            accessEntries!!.addAll(src.accessEntries!!)
        }
    }

    fun formatAccessEntries() {
        formattedAccessEntries = ""

        accessEntries!!.forEach {
            when {
                it.accessType == AccessType.TASK_ACCESS_MANAGEMENT -> formattedAccessEntries += "ACCESS: "
                it.accessType == AccessType.TASKS -> formattedAccessEntries += "TASK: "
                it.accessType == AccessType.TIMESHEETS -> formattedAccessEntries += "TIME: "
                it.accessType == AccessType.OWN_TIMESHEETS -> formattedAccessEntries += "OWN_TIME: "
            }

            if(it.accessSelect){
                formattedAccessEntries += "S"
            }

            if(it.accessInsert){
                formattedAccessEntries += "I"
            }

            if(it.accessUpdate){
                formattedAccessEntries += "U"
            }

            if(it.accessDelete){
                formattedAccessEntries += "D"
            }

            formattedAccessEntries += "; "
        }
    }
}
