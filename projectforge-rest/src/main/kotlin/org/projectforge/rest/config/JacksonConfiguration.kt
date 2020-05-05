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

package org.projectforge.rest.config

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import mu.KotlinLogging
import org.projectforge.business.address.AddressbookDO
import org.projectforge.business.calendar.event.model.ICalendarEvent
import org.projectforge.business.fibu.EmployeeDO
import org.projectforge.business.fibu.KundeDO
import org.projectforge.business.fibu.kost.Kost1DO
import org.projectforge.business.fibu.kost.Kost2DO
import org.projectforge.business.task.TaskDO
import org.projectforge.business.teamcal.admin.model.TeamCalDO
import org.projectforge.business.timesheet.TimesheetDO
import org.projectforge.framework.json.JacksonBaseConfiguration
import org.projectforge.framework.persistence.user.entities.GroupDO
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.framework.persistence.user.entities.TenantDO
import org.projectforge.rest.calendar.ICalendarEventDeserializer
import org.projectforge.rest.calendar.TeamCalDOSerializer
import org.projectforge.rest.dto.*
import org.projectforge.rest.json.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

/**
 * Base configuration of all Spring rest calls. Unknown properties not avoidable by the client might be registered through
 * [registerAllowedUnknownProperties]. For example PFUserDO.fullname is provided as service for the clients, but is
 * an unknown field by PFUserDO.
 */
@Configuration
open class JacksonConfiguration : JacksonBaseConfiguration() {
    companion object {

        init {
            registerAllowedUnknownGlobalProperties("displayName")
            // reminderDuration* will be there after function switchToTimesheet is used:
            registerAllowedUnknownProperties(TimesheetDO::class.java, "reminderDuration", "reminderDurationUnit")
            registerAllowedUnknownProperties(TeamEvent::class.java, "task") // Switch from time sheet.
            registerAllowedUnknownProperties(CalEvent::class.java, "task") // Switch from time sheet.

            registeredDelegatingDeserializer(
                    Customer::class.java,
                    Konto::class.java,
                    Kost1::class.java,
                    Kost2::class.java,
                    Project::class.java)
        }
    }

    @Value("\${projectforge.rest.json.failOnUnknownJsonProperties:false}")
    private var failOnUnknownJsonProperties: Boolean = false

    @Bean
    override fun objectMapper(): ObjectMapper {
        objectMapper?.let { return it }
        val mapper = super.objectMapper()
        if (failOnUnknownJsonProperties) {
            log.warn("Unknown JSON properties are not allowed in REST call, due to configuration in projectforge.properties:projectforge.rest.json.failOnUnknownJsonProperties (OK, but Rest calls may fail).")
        }
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownJsonProperties) // Should be true in development mode!

        module?.let { module ->
            module.setDeserializerModifier(object : BeanDeserializerModifier() {
                override fun modifyDeserializer(config: DeserializationConfig, beanDesc: BeanDescription, deserializer: JsonDeserializer<*>): JsonDeserializer<*>? {
                    getRegisteredDelegatingDeserializers().forEach {
                        if (beanDesc.beanClass == it) {
                            return IdObjectDeserializer(deserializer, it)
                        }
                    }
                    return deserializer
                }
            })

            module.addDeserializer(String::class.java, TextDeserializer())
            module.addDeserializer(java.lang.Integer::class.java, IntDeserializer())
            module.addDeserializer(BigDecimal::class.java, BigDecimalDeserializer())

            module.addSerializer(Kost1DO::class.java, Kost1DOSerializer())
            module.addSerializer(Kost2DO::class.java, Kost2DOSerializer())
            module.addSerializer(KundeDO::class.java, KundeDOSerializer())

            module.addSerializer(PFUserDO::class.java, PFUserDOSerializer())
            module.addDeserializer(PFUserDO::class.java, PFUserDODeserializer())

            module.addSerializer(GroupDO::class.java, GroupDOSerializer())
            module.addSerializer(TaskDO::class.java, TaskDOSerializer())
            module.addSerializer(TenantDO::class.java, TenantDOSerializer())
            module.addSerializer(AddressbookDO::class.java, AddressbookDOSerializer())
            module.addSerializer(EmployeeDO::class.java, EmployeeDOSerializer())

            // Calendar serializers
            module.addSerializer(TeamCalDO::class.java, TeamCalDOSerializer())
            module.addDeserializer(ICalendarEvent::class.java, ICalendarEventDeserializer())
        }
        return mapper
    }
}
