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

package org.projectforge.rest

import org.projectforge.framework.access.AccessDao
import org.projectforge.framework.access.GroupTaskAccessDO
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.rest.dto.GroupTaskAccess
import org.projectforge.ui.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("${Rest.URL}/access")
class GroupAccessPagesRest : AbstractDTOPagesRest<GroupTaskAccessDO, GroupTaskAccess, AccessDao>(AccessDao::class.java, "access.title") {

    override fun transformForDB(dto: GroupTaskAccess): GroupTaskAccessDO {
        val groupTaskAccessDO = GroupTaskAccessDO()
        dto.copyTo(groupTaskAccessDO)
        groupTaskAccessDO.accessEntries = mutableSetOf()
        groupTaskAccessDO.accessEntries!!.addAll(dto.accessEntries!!)
        return groupTaskAccessDO
    }

    override fun transformFromDB(obj: GroupTaskAccessDO, editMode: Boolean): GroupTaskAccess {
        val groupTaskAccess = GroupTaskAccess()
        groupTaskAccess.copyFrom(obj)
        groupTaskAccess.formatAccessEntries()
        return groupTaskAccess
    }

    /**
     * Initializes new memos for adding.
     */
    override fun newBaseDO(request: HttpServletRequest?): GroupTaskAccessDO {
        val groupTaskAccess = super.newBaseDO(request)
        groupTaskAccess.clear()
        return groupTaskAccess
    }

    override val classicsLinkListUrl: String? = "wa/accessList"

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(UITableColumn("task.title", title = "task"))
                        .add(UITableColumn("group.name", title = "group"))
                        .add(UITableColumn("formattedAccessEntries", title = "access.type"))
                        .add(lc, "isRecursive", "description"))

        layout.getTableColumnById("isRecursive").set(
                sortable = false,
                title = "recursive")
                .valueIconMap = mapOf(true to UIIconType.CHECKED)
        return LayoutUtils.processListPage(layout, this)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dto: GroupTaskAccess, userAccess: UILayout.UserAccess): UILayout {
        val layout = super.createEditLayout(dto, userAccess)
                .add(lc, "task")
                .add(UISelect.createGroupSelect(lc, "readonlyAccessUsers", false, "user.assignedGroups"))
                .add(lc, "isRecursive")
                .add(UICustomized("access.table"))
                .add(lc, "description")
        return LayoutUtils.processEditPage(layout, dto, this)
    }
}
