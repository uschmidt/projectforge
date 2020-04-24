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

package org.projectforge.plugins.travel

import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrDataBaseDO
import javax.persistence.*

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
@Entity
class TravelCostAttrDataDO : JpaTabAttrDataBaseDO<TravelCostAttrDO, Int> {
    constructor() : super()

    constructor(parent: TravelCostAttrDO, value: String): super(parent, value)

    @Id
    @GeneratedValue
    @Column(name = "pk")
    override fun getPk(): Int? {
        return pk
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_id", referencedColumnName = "pk")
    override fun getParent(): TravelCostAttrDO {
        return super.getParent()
    }
}