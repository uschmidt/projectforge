/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2022 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.business.orga

import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrDataBaseDO

import javax.persistence.*

@Entity
@DiscriminatorValue("1")
class VisitorbookTimedAttrWithDataDO : VisitorbookTimedAttrDO {

    constructor() : super() {}

    constructor(parent: VisitorbookTimedDO, propertyName: String, type: Char,
                value: String) : super(parent, propertyName, type, value) {}

    constructor(parent: VisitorbookTimedDO) : super(parent) {}

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "parent", targetEntity = VisitorbookTimedAttrDataDO::class, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "datarow")
    override fun getData(): MutableList<JpaTabAttrDataBaseDO<*, Int>>? {
        return super.getData()
    }
}
