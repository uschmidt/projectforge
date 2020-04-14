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

import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrBaseDO
import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrDataBaseDO
import org.hibernate.search.annotations.Indexed
import org.projectforge.business.fibu.kost.Kost2DO
import org.projectforge.common.anots.PropertyInfo
import org.projectforge.framework.persistence.api.Constants
import org.projectforge.framework.persistence.attr.entities.DefaultBaseWithAttrDO
import org.projectforge.framework.persistence.user.entities.PFUserDO
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Transient

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
@Entity
@Indexed
open class TravelKostDO: DefaultBaseWithAttrDO<TravelKostDO>() {

    @PropertyInfo(i18nKey = "plugins.travel.entry.user")
    open var user: PFUserDO? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.reasonOfTravel")
    @get:Column(length = Constants.LENGTH_TEXT)
    open var reasonOfTravel: String? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.startLocation")
    open var startLocation: TravelLocation? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.returnLocation")
    open var returnLocation: TravelLocation? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.destination")
    open var destination: String? = null

    @PropertyInfo(i18nKey = "fibu.kost2")
    open var kost2: Kost2DO? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.beginOfTravel")
    open var beginOfTravel: LocalDate? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.endOfTravel")
    open var endOfTravel: LocalDate? = null

    open var catering: List<CateringDay>? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.hotel")
    open var hotel: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.rentalCar")
    open var rentalCar: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.train")
    open var train: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.flight")
    open var flight: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.kilometers")
    open var kilometers: Int? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption")
    open var assumptionOfCosts: String? = null

    @get:Column(length = Constants.LENGTH_TEXT)
    open var otherAssumptionsOfCosts: String? = null

    @Transient
    override fun getAttrEntityClass(): Class<out JpaTabAttrBaseDO<TravelKostDO, Int>> {
        return TravelKostAttrDO::class.java
    }

    @Transient
    override fun getAttrEntityWithDataClass(): Class<out JpaTabAttrBaseDO<TravelKostDO, Int>> {
        return TravelKostAttrWithDataDO::class.java
    }

    @Transient
    override fun getAttrDataEntityClass(): Class<out JpaTabAttrDataBaseDO<out JpaTabAttrBaseDO<TravelKostDO, Int>, Int>> {
        return TravelKostAttrDataDO::class.java
    }

    override fun createAttrEntity(key: String, type: Char, value: String): JpaTabAttrBaseDO<TravelKostDO, Int> {
        return TravelKostAttrDO(this, key, type, value)
    }

    override fun createAttrEntityWithData(key: String, type: Char, value: String): JpaTabAttrBaseDO<TravelKostDO, Int> {
        return TravelKostAttrWithDataDO(this, key, type, value)
    }


}
