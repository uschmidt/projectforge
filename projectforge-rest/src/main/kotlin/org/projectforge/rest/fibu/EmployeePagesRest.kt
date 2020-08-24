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

package org.projectforge.rest.fibu

import org.projectforge.business.fibu.EmployeeDO
import org.projectforge.business.fibu.EmployeeDao
import org.projectforge.business.fibu.kost.Kost1DO
import org.projectforge.business.fibu.kost.Kost1Dao
import org.projectforge.framework.persistence.api.BaseSearchFilter
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.rest.dto.Employee
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("${Rest.URL}/employee")
class EmployeePagesRest : AbstractDTOPagesRest<EmployeeDO, Employee, EmployeeDao>(EmployeeDao::class.java, "fibu.employee.title") {

    @Autowired
    private lateinit var kost1Dao: Kost1Dao

    override fun transformFromDB(obj: EmployeeDO, editMode: Boolean): Employee {
        val employee = Employee()
        employee.copyFrom(obj)
        return employee
    }

    override fun transformForDB(dto: Employee): EmployeeDO {
        val employeeDO = this.baseDao.newInstance()
        dto.copyTo(employeeDO)

        employeeDO.kost1 = kost1Dao.getKost1(dto.nummernkreis, dto.bereich, dto.teilbereich, dto.endziffer)

        return employeeDO
    }

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(UITableColumn("user.lastname", "name"))
                        .add(UITableColumn("user.firstname", "firstName"))
                        .add(lc, "status", "staffNumber")
                        .add(UITableColumn("kost1.displayName", "fibu.kost1"))
                        .add(lc, "position", "abteilung", "eintrittsDatum", "austrittsDatum", "comment"))
        layout.getTableColumnById("eintrittsDatum").formatter = Formatter.DATE
        layout.getTableColumnById("austrittsDatum").formatter = Formatter.DATE
        return LayoutUtils.processListPage(layout, this)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dto: Employee, userAccess: UILayout.UserAccess): UILayout {
        val costNumber = UICustomized("cost.number")
                .add("nummernkreis", dto.nummernkreis)
                .add("bereich", dto.bereich)
                .add("teilbereich", dto.teilbereich)
                .add("endziffer", dto.endziffer)

        val layout = super.createEditLayout(dto, userAccess)
                .add(UIRow()
                        .add(UICol()
                                .add(lc, "user")
                                .add(costNumber)
                                .add(lc, "abteilung", "position"))
                        .add(UICol()
                                .add(lc, "staffNumber", "weeklyWorkingHours", "eintrittsDatum", "austrittsDatum")))
                .add(UIRow()
                        .add(UICol().add(lc, "street", "zipCode", "city"))
                        .add(UICol().add(lc, "country", "state"))
                        .add(UICol().add(lc, "birthday", "gender"))
                        .add(UICol().add(lc, "accountHolder", "iban", "bic")))
                .add(UIRow()
                        .add(UICol().add(lc, "status")))
                .add(UILabel("TODO: Custom properties here"))
                .add(UIRow()
                        .add(UICol().add(lc, "comment")))
        return LayoutUtils.processEditPage(layout, dto, this)
    }

    override val autoCompleteSearchFields = arrayOf("user.username", "user.firstname", "user.lastname", "user.email")

    override fun queryAutocompleteObjects(request: HttpServletRequest, filter: BaseSearchFilter): MutableList<EmployeeDO> {
        val list = baseDao.internalGetEmployeeList(filter, showOnlyActiveEntries = true).toMutableList()
        val today = LocalDate.now()
        list.removeIf { it.austrittsDatum?.isBefore(today) == true || it.isDeleted } // Remove deactivated users when returning all. Show deactivated users only if search string is given.
        return list
    }
}
