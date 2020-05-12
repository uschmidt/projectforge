package org.projectforge.rest.dto

import org.projectforge.framework.configuration.entities.ConfigurationDO

class Configuration(
        var parameter: String? = null,
        var stringValue: String? = null,
        var description: String? = null,
        var descriptionI18nKey: String? = null
): BaseDTO<ConfigurationDO>() {


}