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

package org.projectforge.plugins.travel.rest

import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.plugins.travel.TravelKostDO
import org.projectforge.plugins.travel.TravelKostDao
import org.projectforge.plugins.travel.dto.TravelKost
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.ui.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */

@RestController
@RequestMapping("${Rest.URL}/travelCost")
class TravelCostPagesRest : AbstractDTOPagesRest<TravelKostDO, TravelKost, TravelKostDao>(TravelKostDao::class.java, "plugins.TravelKost.title") {

    override fun transformFromDB(obj: TravelKostDO, editMode: Boolean): TravelKost {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun transformForDB(dto: TravelKost): TravelKostDO {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Initializes new TravelKosts for adding.
     */
    override fun newBaseDO(request: HttpServletRequest?): TravelKostDO {
        val travelKost = super.newBaseDO(request)
        travelKost.user = ThreadLocalUserContext.getUser()
        return travelKost
    }

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(lc, "user.name"))
        return LayoutUtils.processListPage(layout, this)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dto: TravelKost, userAccess: UILayout.UserAccess): UILayout {
        val location = UIInput("location", lc).enableAutoCompletion(this)
        val layout = super.createEditLayout(dto, userAccess)
                .add(UISelect.createUserSelect(lc, "user", false, "plugins.travel.entry.user"))
        //additionalLabel = "access.users",
        return LayoutUtils.processEditPage(layout, dto, this)
    }
}
