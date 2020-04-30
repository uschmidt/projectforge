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

import com.fasterxml.jackson.annotation.JsonIgnore
import de.micromata.genome.db.jpa.history.api.NoHistory
import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrBaseDO
import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrDataBaseDO
import org.hibernate.search.annotations.Field
import org.hibernate.search.annotations.Indexed
import org.projectforge.business.fibu.kost.Kost2DO
import org.projectforge.common.anots.PropertyInfo
import org.projectforge.framework.jcr.AttachmentsInfo
import org.projectforge.framework.persistence.api.Constants
import org.projectforge.framework.persistence.attr.entities.DefaultBaseWithAttrDO
import org.projectforge.framework.persistence.user.entities.PFUserDO
import java.time.LocalDate
import javax.persistence.*

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_TRAVEL")
open class TravelCostDO: DefaultBaseWithAttrDO<TravelCostDO>(), AttachmentsInfo {

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

    //open var catering: MutableList<CateringDay>? = null

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
    @get:Column(length = Constants.LENGTH_TEXT)
    open var assumptionOfCosts: String? = null

    @get:Column(length = Constants.LENGTH_TEXT)
    open var otherAssumptionsOfCosts: String? = null

    @JsonIgnore
    @Field
    @field:NoHistory
    @get:Column(length = 10000, name = "attachments_names")
    override var attachmentsNames: String? = null

    @JsonIgnore
    @Field
    @field:NoHistory
    @get:Column(length = 10000, name = "attachments_ids")
    override var attachmentsIds: String? = null

    @JsonIgnore
    @field:NoHistory
    @get:Column(length = 10000, name = "attachments_size")
    override var attachmentsSize: Int? = null

    @PropertyInfo(i18nKey = "attachment")
    @JsonIgnore
    @get:Column(length = 10000, name = "attachments_last_user_action")
    override var attachmentsLastUserAction: String? = null

    @Transient
    override fun getAttrEntityClass(): Class<out JpaTabAttrBaseDO<TravelCostDO, Int>> {
        return TravelCostAttrDO::class.java
    }

    @Transient
    override fun getAttrEntityWithDataClass(): Class<out JpaTabAttrBaseDO<TravelCostDO, Int>> {
        return TravelCostAttrWithDataDO::class.java
    }

    @Transient
    override fun getAttrDataEntityClass(): Class<out JpaTabAttrDataBaseDO<out JpaTabAttrBaseDO<TravelCostDO, Int>, Int>> {
        return TravelCostAttrDataDO::class.java
    }

    override fun createAttrEntity(key: String, type: Char, value: String): JpaTabAttrBaseDO<TravelCostDO, Int> {
        return TravelCostAttrDO(this, key, type, value)
    }

    override fun createAttrEntityWithData(key: String, type: Char, value: String): JpaTabAttrBaseDO<TravelCostDO, Int> {
        return TravelCostAttrWithDataDO(this, key, type, value)
    }


}
