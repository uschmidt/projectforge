package org.projectforge.rest.dto

import org.projectforge.framework.configuration.ConfigurationType
import org.projectforge.framework.configuration.entities.ConfigurationDO
import org.projectforge.framework.i18n.translate
import java.math.BigDecimal
import java.util.*

class Configuration(
        var parameter: String? = null,
        var stringValue: String? = null,
        var configurationType: ConfigurationType? = null,
        var intValue: Int? = null,
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