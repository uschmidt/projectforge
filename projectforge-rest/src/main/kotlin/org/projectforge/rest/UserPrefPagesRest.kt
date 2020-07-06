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

import org.projectforge.business.user.UserPrefDao
import org.projectforge.framework.persistence.user.entities.UserPrefDO
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.rest.dto.UserPref
import org.projectforge.ui.LayoutUtils
import org.projectforge.ui.UILayout
import org.projectforge.ui.UITable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${Rest.URL}/userPref")
class UserPrefPagesRest: AbstractDTOPagesRest<UserPrefDO, UserPref, UserPrefDao>(UserPrefDao::class.java, "userPref.title") {

    override fun transformForDB(dto: UserPref): UserPrefDO {
        val userPrefDO = UserPrefDO()
        dto.copyTo(userPrefDO)
        return userPrefDO
    }

    override fun transformFromDB(obj: UserPrefDO, editMode: Boolean): UserPref {
        val userPref = UserPref()
        userPref.copyFrom(obj)
        return userPref
    }

    override val classicsLinkListUrl: String? = "wa/userPrefList"

    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(lc, "area", "name", "user", "lastUpdate"))
        return LayoutUtils.processListPage(layout, this)
    }

    override fun createEditLayout(dto: UserPref, userAccess: UILayout.UserAccess): UILayout {
        val layout = super.createEditLayout(dto, userAccess)
                .add(lc, "stringValue")
        return LayoutUtils.processEditPage(layout, dto, this)
    }



}
