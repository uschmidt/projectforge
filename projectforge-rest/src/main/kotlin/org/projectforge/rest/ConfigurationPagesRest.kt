package org.projectforge.rest

import org.projectforge.framework.configuration.ConfigurationDao
import org.projectforge.framework.configuration.entities.ConfigurationDO
import org.projectforge.framework.i18n.translate
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTOPagesRest
import org.projectforge.rest.dto.Configuration
import org.projectforge.ui.LayoutUtils
import org.projectforge.ui.UILayout
import org.projectforge.ui.UITable
import org.projectforge.ui.UITableColumn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${Rest.URL}/configuration")
class ConfigurationPagesRest: AbstractDTOPagesRest<ConfigurationDO, Configuration,  ConfigurationDao>(ConfigurationDao::class.java, "administration.configuration.title") {

    override fun transformForDB(dto: Configuration): ConfigurationDO {
        val configurationDO = ConfigurationDO()
        dto.copyTo(configurationDO)
        return configurationDO
    }

    override fun transformFromDB(obj: ConfigurationDO, editMode: Boolean): Configuration {
        val configuration = Configuration()
        configuration.copyFrom(obj)
        return configuration
    }

    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.createUIResultSetTable()
                        .add(lc, "parameter")
                        .add(UITableColumn("value", title = "administration.configuration.value"))
                        .add(UITableColumn("description", title = "description")))
        return LayoutUtils.processListPage(layout, this)
    }

    override fun createEditLayout(dto: Configuration, userAccess: UILayout.UserAccess): UILayout {
        val layout = super.createEditLayout(dto, userAccess)
                .add(lc, "value")
        return LayoutUtils.processEditPage(layout, dto, this)
    }



}