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

import org.projectforge.Const
import org.projectforge.menu.builder.MenuCreator
import org.projectforge.menu.builder.MenuItemDef
import org.projectforge.menu.builder.MenuItemDefId
import org.projectforge.plugins.core.AbstractPlugin
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
class TravelCostPlugin : AbstractPlugin(ID,"Travel Cost Plugin", "Plugin to manage travel costs") {

    @Autowired
    private lateinit var travelCostDao: TravelCostDao

    @Autowired
    private lateinit var menuCreator: MenuCreator

    override fun initialize() {
        // Register it:
        register(ID, travelCostDao::class.java, travelCostDao, "plugins.travel")

        // Define the access management:
        registerRight(TravelCostRight(accessChecker))

        menuCreator.add(MenuItemDefId.MISC, MenuItemDef(info.id, "plugins.travel.menu", "${Const.REACT_APP_PATH}travelCost"))

        // All the i18n stuff:
        addResourceBundle(RESOURCE_BUNDLE_NAME)
    }

    companion object {
        const val ID = "travel"
        const val RESOURCE_BUNDLE_NAME = "TravelI18nResources"
    }
}
