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

import org.projectforge.business.fibu.EmployeeDO
import org.projectforge.business.fibu.EmployeeStatus
import org.projectforge.business.fibu.Gender
import org.projectforge.framework.persistence.user.entities.PFUserDO
import java.math.BigDecimal
import java.time.LocalDate

class Employee(id: Int? = null,
               displayName: String? = null,
               var user: User? = User(),
               var nummernkreis: Int = 0,
               var bereich: Int = 0,
               var teilbereich: Int = 0,
               var endziffer: Int = 0,
               var status: EmployeeStatus? = null,
               var position: String? = null,
               var eintrittsDatum: LocalDate? = null,
               var austrittsDatum: LocalDate? = null,
               var abteilung: String? = null,
               var staffNumber: String? = null,
               var urlaubstage: Int? = null,
               var weeklyWorkingHours: BigDecimal? = null,
               var birthday: LocalDate? = null,
               var accountHolder: String? = null,
               var iban: String? = null,
               var bic: String? = null,
               var gender: Gender? = null,
               var street: String? = null,
               var zipCode: String? = null,
               var city: String? = null,
               var country: String? = null,
               var state: String? = null,
               var comment: String? = null
) : BaseDTODisplayObject<EmployeeDO>(id, displayName = displayName){

    override fun copyFrom(src: EmployeeDO) {
        super.copyFrom(src)

        if(src.user != null){
            this.user!!.copyFrom(src.user!!)
        }

        val kost1 = src.kost1
        nummernkreis = kost1?.nummernkreis ?: 0
        bereich = kost1?.bereich ?: 0
        teilbereich = kost1?.teilbereich ?: 0
        endziffer = kost1?.endziffer ?: 0
    }
}
