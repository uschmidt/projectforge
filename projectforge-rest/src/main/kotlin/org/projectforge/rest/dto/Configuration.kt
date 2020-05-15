package org.projectforge.rest.dto

import org.projectforge.framework.configuration.entities.ConfigurationDO
import org.projectforge.framework.i18n.translate

class Configuration(
        var parameter: String? = null,
        var stringValue: String? = "",
        var description: String? = null,
        var descriptionI18nKey: String? = null
): BaseDTO<ConfigurationDO>() {

    override fun copyFrom(src: ConfigurationDO) {
        super.copyFrom(src)

        if(src.value != null){
            this.stringValue = src.value.toString()
        }
        this.descriptionI18nKey = src.descriptionI18nKey
        this.description = translate(src.descriptionI18nKey)
    }
}