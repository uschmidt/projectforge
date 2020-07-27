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

import org.projectforge.business.task.TaskDO
import org.projectforge.business.teamcal.admin.model.TeamCalDO
import org.projectforge.framework.configuration.ConfigurationType
import org.projectforge.framework.configuration.entities.ConfigurationDO
import org.projectforge.framework.i18n.translate
import java.math.BigDecimal
import java.util.*

class Configuration(
        var parameter: String? = null,
        var translatedParameter: String? = null,
        var stringValue: String? = null,
        var configurationType: ConfigurationType? = null,
        var intValue: Int? = null,
        var calendar: TeamCalDO? = null,
        var task: TaskDO? = null,
        var booleanValue: Boolean? = null,
        var floatValue: BigDecimal? = null,
        var timeZone: TimeZone? = null,
        var description: String? = null,
        var descriptionI18nKey: String? = null,
        var displayValue: String? = ""
): BaseDTO<ConfigurationDO>() {

    override fun copyFrom(src: ConfigurationDO) {
        super.copyFrom(src)

        this.descriptionI18nKey = src.descriptionI18nKey
        this.description = translate(src.descriptionI18nKey)

        this.parameter = "administration.configuration.param.$parameter"

        this.translatedParameter = translate(parameter)

        when (src.configurationType) {
            ConfigurationType.BOOLEAN -> displayValue = src.booleanValue.toString()
            ConfigurationType.INTEGER -> displayValue = src.intValue.toString()
            ConfigurationType.STRING -> displayValue = src.stringValue
            ConfigurationType.TEXT -> displayValue = src.stringValue
            ConfigurationType.FLOAT -> displayValue = src.floatValue.toString()
            ConfigurationType.PERCENT -> displayValue = src.floatValue.toString()
            ConfigurationType.TIME_ZONE -> displayValue = src.timeZoneId
            //ConfigurationType.CALENDAR -> layout.add()
            //ConfigurationType.TASK -> layout.add()
            else -> ""
        }
    }
}
