package org.projectforge.rest

import org.projectforge.framework.configuration.ConfigurationDao
import org.projectforge.framework.configuration.ConfigurationType
import org.projectforge.framework.configuration.entities.ConfigurationDO
import org.projectforge.framework.i18n.translate
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.rest.dto.Configuration
import org.projectforge.ui.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${Rest.URL}/configuration")
class ConfigurationPagesRest: AbstractDTOPagesRest<ConfigurationDO, Configuration,  ConfigurationDao>(ConfigurationDao::class.java, "administration.configuration.title") {

    override fun transformForDB(dto: Configuration): ConfigurationDO {
        val configurationDO = ConfigurationDO()
        dto.copyTo(configurationDO)
        if(dto.configurationType == ConfigurationType.BOOLEAN){
            configurationDO.stringValue = dto.booleanValue.toString()
        }
        return configurationDO
    }

    override fun transformFromDB(obj: ConfigurationDO, editMode: Boolean): Configuration {
        val configuration = Configuration()
        configuration.copyFrom(obj)
        configuration.booleanValue = obj.booleanValue
        if(obj.configurationType == ConfigurationType.TIME_ZONE){
            configuration.timeZone = obj.timeZone
        }
        return configuration
    }

    override val classicsLinkListUrl: String? = "wa/configuration"

    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(lc, "parameter")
                        .add(UITableColumn("displayValue", title = "administration.configuration.value"))
                        .add(UITableColumn("description", title = "description")))
        return LayoutUtils.processListPage(layout, this)
    }

    // TODO: How to prevent adding more configs?
    // TODO: CALENDAR and TASK missing
    override fun createEditLayout(dto: Configuration, userAccess: UILayout.UserAccess): UILayout {
        val layout = super.createEditLayout(dto, userAccess)

        when (dto.configurationType) {
            ConfigurationType.BOOLEAN -> layout.add(UICheckbox("booleanValue", lc, label = "administration.configuration.value"))
            ConfigurationType.INTEGER -> layout.add(lc, "intValue")
            ConfigurationType.STRING -> layout.add(lc, "stringValue")
            ConfigurationType.TEXT -> layout.add(lc, "stringValue")
            ConfigurationType.FLOAT -> layout.add(lc, "floatValue")
            ConfigurationType.PERCENT -> layout.add(lc, "floatValue")
            ConfigurationType.TIME_ZONE -> layout.add(lc, "timeZone")
            //ConfigurationType.CALENDAR -> layout.add()
            //ConfigurationType.TASK -> layout.add()
            else -> ""
        }

        return LayoutUtils.processEditPage(layout, dto, this)
    }



}