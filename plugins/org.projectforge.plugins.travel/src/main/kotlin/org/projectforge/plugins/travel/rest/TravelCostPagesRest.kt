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

import org.projectforge.framework.i18n.translate
import org.projectforge.framework.jcr.AttachmentsService
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.plugins.travel.TravelCostDO
import org.projectforge.plugins.travel.TravelCostDao
import org.projectforge.plugins.travel.dto.TravelCost
import org.projectforge.rest.config.JacksonConfiguration
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import kotlin.math.abs
import jdk.jfr.Timespan.MILLISECONDS
import org.projectforge.rest.dto.PostData
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.DAYS



// TODO: Add jcr support (see ContractPagesRest/jcr and attachment*)
/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
@RestController
@RequestMapping("${Rest.URL}/travelCost")
class TravelCostPagesRest : AbstractDTOPagesRest<TravelCostDO, TravelCost, TravelCostDao>(TravelCostDao::class.java, "plugins.travel.entry.title") {

    @Autowired
    private lateinit var attachmentsService: AttachmentsService

    override fun transformFromDB(obj: TravelCostDO, editMode: Boolean): TravelCost {
        val travelCost = TravelCost()
        travelCost.copyFrom(obj)
        return travelCost
    }

    override fun transformForDB(dto: TravelCost): TravelCostDO {
        val travelCostDO = TravelCostDO()
        dto.copyTo(travelCostDO)
        return travelCostDO
    }

    @PostConstruct
    private fun postConstruct() {
        /**
         * Enable attachments for this entity.
         */
        enableJcr()
        JacksonConfiguration.registerAllowedUnknownProperties(TravelCost::class.java, "statusAsString")
    }

    /**
     * Initializes new TravelCosts for adding.
     */
    override fun newBaseDO(request: HttpServletRequest?): TravelCostDO {
        val travelCost = super.newBaseDO(request)
        //travelCost.user = ThreadLocalUserContext.getUser()
        return travelCost
    }

    override fun onAfterSaveOrUpdate(request: HttpServletRequest, obj: TravelCostDO, postData: PostData<TravelCost>) {
        super.onAfterSaveOrUpdate(request, obj, postData)
        print("")
    }

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(UITableColumn("employee.user.displayName", "plugins.travel.entry.user"))
                        .add(lc, "employee.staffNumber", "beginOfTravel", "endOfTravel", "destination", "kilometers")
                        .add(UITableColumn("formattedRefundByKilometer",
                                "TODO"))
                        .add(UITableColumn("formattedRefundByKilometerPassenger",
                                "TODO"))
                        .add(UITableColumn("totalRefund",
                                "TODO"))
                        .add(UITableColumn("cateringPrice",
                                "TODO"))
                        .add(UITableColumn("cateringNumber",
                                "TODO"))
                        .add(UITableColumn("cateringCost",
                                "TODO")))
        return LayoutUtils.processListPage(layout, this)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dto: TravelCost, userAccess: UILayout.UserAccess): UILayout {
        //val location = UIInput("location", lc).enableAutoCompletion(this)
        val dayRange = UICustomized("dayRange")
                .add("startDateId", "beginOfTravel")
                .add("endDateId", "endOfTravel")
                .add("label", translate("timePeriod"))
        val layout = super.createEditLayout(dto, userAccess)
                .add(UISelect.createEmployeeSelect(lc, "employee", false, "plugins.travel.entry.user"))
                .add(lc, "reasonOfTravel", "destination")
                // TODO: Maybe a Kost2 Selection?
                //.add(UICustomized("plugins.travel.edit.kost2"))
                .add(dayRange)
                .add(lc, "startLocation", "returnLocation", "kilometers")
                .add(UICheckbox("hotel", lc))
                .add(UICheckbox("rentalCar", lc))
                .add(UICheckbox("train", lc))
                .add(UICheckbox("flight", lc))
                .add(lc, "assumptionOfCosts")
                .add(UICheckbox("receiptsCompletelyAvailable", lc))

        //additionalLabel = "access.users",
        return LayoutUtils.processEditPage(layout, dto, this)
    }
}
