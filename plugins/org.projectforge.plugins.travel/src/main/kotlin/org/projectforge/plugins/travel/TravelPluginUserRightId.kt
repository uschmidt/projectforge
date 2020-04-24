package org.projectforge.plugins.travel

import org.projectforge.framework.persistence.api.IUserRightId
import org.projectforge.framework.persistence.api.RightRightIdProviderService

/**
 * @param id Must be unique (including all plugins).
 * @param orderString For displaying the rights in e. g. UserEditPage in the correct order.
 * @param i18nKey
 */
enum class TravelPluginUserRightId
            (override val id: String,
             override val orderString: String,
             override val i18nKey: String) : IUserRightId {
    PLUGIN_TRAVEL("PLUGIN_TRAVEL", "travel20", "plugins.travel.entry");

    class ProviderService : RightRightIdProviderService {
        override fun getUserRightIds(): Collection<IUserRightId> {
            return listOf(*values())
        }
    }

    override fun toString(): String {
        return id
    }

    override operator fun compareTo(o: IUserRightId?): Int {
        return this.compareTo(o)
    }
}