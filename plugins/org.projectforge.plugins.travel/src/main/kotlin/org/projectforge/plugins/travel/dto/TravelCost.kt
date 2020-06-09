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

import org.projectforge.business.fibu.kost.Kost2DO
import org.projectforge.framework.jcr.Attachment
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.plugins.travel.TravelCostDO
import org.projectforge.plugins.travel.TravelLocation
import org.projectforge.rest.dto.AttachmentsSupport
import org.projectforge.rest.dto.BaseDTO
import org.projectforge.rest.dto.Kost2
import java.time.LocalDate

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
class TravelCost(id: Int? = null,
                 displayName: String? = null,
                 var user: PFUserDO? = null,
                 var reasonOfTravel: String? = null,
                 var startLocation: TravelLocation? = null,
                 var returnLocation: TravelLocation? = null,
                 var destination: String? = null,
                 var kost2: Kost2? = Kost2(),
                 var beginOfTravel: LocalDate? = null,
                 var endOfTravel: LocalDate? = null,
                 var hotel: Boolean = false,
                 var rentalCar: Boolean = false,
                 var train: Boolean = false,
                 var flight: Boolean = false,
                 var kilometers: Int? = null,
                 override var attachments: List<Attachment>? = null): BaseDTO<TravelCostDO>(), AttachmentsSupport {

    override fun copyFrom(src: TravelCostDO) {
        super.copyFrom(src)

        /*if(src.kost2 != null){
            this.kost2!!.copyFrom(src.kost2!!)
        }*/
    }
}
