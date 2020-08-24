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
import org.hibernate.search.annotations.Field
import org.hibernate.search.annotations.Indexed
import org.hibernate.search.annotations.IndexedEmbedded
import org.projectforge.business.fibu.EmployeeDO
import org.projectforge.business.fibu.ProjektDO
import org.projectforge.business.fibu.kost.Kost2DO
import org.projectforge.common.anots.PropertyInfo
import org.projectforge.framework.jcr.AttachmentsInfo
import org.projectforge.framework.persistence.api.Constants
import org.projectforge.framework.persistence.entities.DefaultBaseDO
import org.projectforge.framework.persistence.user.entities.UserPrefDO
import java.util.*
import javax.persistence.*

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
@Entity
@Indexed
@Table(name = "t_plugin_travel",
        uniqueConstraints = [UniqueConstraint(columnNames = ["employee_id"])],
        indexes = [
            javax.persistence.Index(name = "idx_fk_t_plugin_travel_projekt_id", columnList = "projekt_id"),
            javax.persistence.Index(name = "idx_fk_t_plugin_travel_employee_id", columnList = "employee_id")
        ])
open class TravelCostDO: DefaultBaseDO(), AttachmentsInfo {
    @JsonIgnore
    private val log = org.slf4j.LoggerFactory.getLogger(TravelCostDO::class.java)

    @PropertyInfo(i18nKey = "plugins.travel.entry.user")
    @IndexedEmbedded(depth = 1)
    @get:ManyToOne(fetch = FetchType.EAGER)
    @get:JoinColumn(name = "employee_id", nullable = false)
    open var employee: EmployeeDO? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.reasonOfTravel")
    @get:Column(name = "reason_of_travel", length = Constants.LENGTH_TEXT)
    open var reasonOfTravel: String? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.startLocation")
    @get:Column(name = "start_location")
    open var startLocation: TravelLocation? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.returnLocation")
    @get:Column(name = "return_location")
    open var returnLocation: TravelLocation? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.destination")
    @get:Column(name = "destination")
    open var destination: String? = null

    @PropertyInfo(i18nKey = "fibu.projekt")
    @IndexedEmbedded(depth = 1)
    @get:ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.MERGE])
    @get:JoinColumn(name = "projekt_id", nullable = true)
    open var projekt: ProjektDO? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.beginOfTravel")
    @get:Column(name = "begin_of_travel")
    open var beginOfTravel: Date? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.endOfTravel")
    @get:Column(name = "end_of_travel")
    open var endOfTravel: Date? = null

    /**
     * The value as string representation (e. g. json).
     */
    @get:Column(name = "value_string", length = 100000) // 100.000, should be space enough.
    var cateringValueString: String? = null

    /**
     * The value as object (deserialized from json).
     * The list of cateringDays
     */
    @get:Transient
    var cateringValueObject: Any? = null

    /**
     * The type of the value (class name). It's not of type class because types are may-be refactored or removed.
     */
    @get:Column(name = "value_type", length = 1000)
    var cateringValueTypeString: String? = null

    /**
     * [valueTypeString] as class or null, if [valueTypeString] is null.
     */
    val valueType: Class<*>?
        @Transient
        get() {
            try {
                return if (cateringValueTypeString.isNullOrBlank())
                    null
                else Class.forName(cateringValueTypeString)
            } catch (ex: ClassNotFoundException) {
                log.error("Can't get value type from '$cateringValueTypeString'. Class not found (old incompatible ProjectForge version)?")
                return null
            }
        }

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.hotel")
    @get:Column(name = "hotel")
    open var hotel: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.rentalCar")
    @get:Column(name = "rental_car")
    open var rentalCar: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.train")
    @get:Column(name = "train")
    open var train: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.flight")
    @get:Column(name = "flight")
    open var flight: Boolean = false

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption.kilometers")
    @get:Column(name = "kilometers")
    open var kilometers: Int? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.costAssumption")
    @get:Column(name = "assumption_of_costs", length = Constants.LENGTH_TEXT)
    open var assumptionOfCosts: String? = null

    @PropertyInfo(i18nKey = "plugins.travel.entry.receiptsComplete")
    @get:Column(name = "receipts_completely_available")
    open var receiptsCompletelyAvailable: Boolean = false

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


}
