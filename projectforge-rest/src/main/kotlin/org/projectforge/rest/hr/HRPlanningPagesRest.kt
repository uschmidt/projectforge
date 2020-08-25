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

package org.projectforge.rest.hr

import org.projectforge.business.humanresources.HRPlanningDO
import org.projectforge.business.humanresources.HRPlanningDao
import org.projectforge.business.humanresources.HRPlanningEntryDO
import org.projectforge.business.humanresources.HRPlanningEntryDao
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDOPagesRest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.rest.dto.HRPlanning
import org.projectforge.rest.dto.HRPlanningEntry
import org.projectforge.ui.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${Rest.URL}/hrPlanning")
class HRPlanningPagesRest : AbstractDTOPagesRest<HRPlanningDO, HRPlanning, HRPlanningDao>(HRPlanningDao::class.java, "hr.planning.title") {
    override fun transformForDB(dto: HRPlanning): HRPlanningDO {
        val hrPlanningDO = HRPlanningDO()
        dto.copyTo(hrPlanningDO)
        hrPlanningDO.entries = mutableListOf()
        hrPlanningDO.entries!!.addAll(dto.entries!!)
        hrPlanningDO.entries!!.forEach {
            it.planning = hrPlanningDO
        }
        return hrPlanningDO
    }

    override fun transformFromDB(obj: HRPlanningDO, editMode: Boolean): HRPlanning {
        val hrPlanning = HRPlanning()
        hrPlanning.copyFrom(obj)
        return hrPlanning
    }

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(lc, "user")
                        .add(UITableColumn("totalHours", title = "hr.planning.sum"))
                        .add(UITableColumn("totalUnassignedHours", title = "rest")))
        return LayoutUtils.processListPage(layout, this)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dto: HRPlanning, userAccess: UILayout.UserAccess): UILayout {
        val select: UISelect<Int> = UISelect("entry.probability",
                label = "hr.planning.probability.short")

        val selectValues = mutableSetOf<UISelectValue<Int>>()
        selectValues.add(UISelectValue(25, "25%"))
        selectValues.add(UISelectValue(50, "50%"))
        selectValues.add(UISelectValue(75, "75%"))
        selectValues.add(UISelectValue(95, "95%"))
        selectValues.add(UISelectValue(100, "100%"))


        val layout = super.createEditLayout(dto, userAccess)
                .add(UISelect.createUserSelect(lc, "user", false))
                .add(lc, "week")
                .add(UIList(lc, "entries", "entry", positionLabel = "menu.addNewEntry")
                        .add(UIRow()
                                .add(UICol()
                                        .add(lc, "entry.status")
                                        .add(UISelect.createProjectSelect(lc, "entry.projekt", multi = false))))
                        .add(UIRow()
                                .add(UICol()
                                        .add(lc, "entry.priority"))
                                .add(UICol()
                                        .add(select.buildFromList(selectValues))))
                        .add(UIRow()
                                .add(UICol()
                                        .add(lc, "entry.unassignedHours", "entry.mondayHours", "entry.tuesdayHours", "entry.wednesdayHours", "entry.thursdayHours", "entry.fridayHours", "entry.weekendHours"))
                                .add(UICol()
                                        .add(lc, "entry.description"))))
        return LayoutUtils.processEditPage(layout, dto, this)
    }
}
