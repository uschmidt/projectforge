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

package org.projectforge.plugins.travel.dto

import org.projectforge.framework.jcr.Attachment
import org.projectforge.framework.utils.NumberFormatter
import org.projectforge.plugins.travel.CateringDay
import org.projectforge.plugins.travel.TravelCostDO
import org.projectforge.plugins.travel.TravelLocation
import org.projectforge.rest.dto.AttachmentsSupport
import org.projectforge.rest.dto.BaseDTO
import org.projectforge.rest.dto.Employee
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
class TravelCost(id: Int? = null,
                 displayName: String? = null,
                 var employee: Employee? = Employee(),
                 var reasonOfTravel: String? = null,
                 var startLocation: TravelLocation? = null,
                 var returnLocation: TravelLocation? = null,
                 var destination: String? = null,
                 //var kost2: Kost2? = Kost2(),
                 var beginOfTravel: java.util.Date? = null,
                 var endOfTravel: java.util.Date? = null,
                 var catering: CateringDay? = null,
                 var hotel: Boolean = false,
                 var rentalCar: Boolean = false,
                 var train: Boolean = false,
                 var flight: Boolean = false,
                 var kilometers: Int? = null,
                 override var attachments: List<Attachment>? = null): BaseDTO<TravelCostDO>(), AttachmentsSupport {

    val refundByKilometer = 0.30
    val refundByKilometerPassenger = 0.02
    val cateringCostPerPoint = 5.6
    var formattedRefundByKilometer = "0.00 €"
    var formattedRefundByKilometerPassenger = "0.00 €"
    var totalRefund = "0.00 €"
    var cateringPrice = "0.00 €"
    var rkPauschale = "0.00 €"
    var cateringNumber = "0"
    var cateringCost = "0.00 €"

    override fun copyFrom(src: TravelCostDO) {
        super.copyFrom(src)

        /*if(src.kost2 != null){
            this.kost2!!.copyFrom(src.kost2!!)
        }*/

        if(src.employee != null){
            this.employee!!.copyFrom(src.employee!!)
        }

        if(kilometers != null){
            val value1 = kilometers!! * refundByKilometer
            val value2 = kilometers!! * refundByKilometerPassenger
            formattedRefundByKilometer = NumberFormatter.formatCurrency(value1) + " €"
            formattedRefundByKilometerPassenger = NumberFormatter.formatCurrency(value2) + " €"
            totalRefund = NumberFormatter.formatCurrency(value1 + value2) + " €"
        }


        val diffInMillies = abs(endOfTravel!!.time - beginOfTravel!!.time)
        val diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS)

        if(diff >= 24){
            cateringPrice = NumberFormatter.formatCurrency(28) + " €"
            rkPauschale = NumberFormatter.formatCurrency(10) + " €"
        } else if (diff >= 8){
            cateringPrice = NumberFormatter.formatCurrency(14) + " €"
            rkPauschale = NumberFormatter.formatCurrency(10) + " €"
        }

        if(catering != null){
            cateringNumber = "" + catering!!.getNumber()
            cateringCost = NumberFormatter.formatCurrency(catering!!.getNumber() * cateringCostPerPoint) + " €"
        }
    }
}
