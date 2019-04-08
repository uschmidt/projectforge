package org.projectforge.rest.core

import org.projectforge.framework.persistence.api.BaseSearchFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.servlet.http.HttpSession

/**
 * For persisting list filters.
 */
@Component
class ListFilterService {
    private val log = org.slf4j.LoggerFactory.getLogger(ListFilterService::class.java)

    @Autowired
    private lateinit var userXmlPreferencesService: RestUserXmlPreferencesService

    fun getSearchFilter(session: HttpSession, filterClazz: Class<out BaseSearchFilter>): BaseSearchFilter {
        val filter = userXmlPreferencesService.getEntry(session, filterClazz.name + ":Filter")
        if (filter != null) {
            if (filter.javaClass == filterClazz) {
                try {
                    return filter as BaseSearchFilter
                } catch (ex: ClassCastException) {
                    // No output needed, info message follows:
                }
                // Probably a new software release results in an incompability of old and new filter format.
                log.info(
                        "Could not restore filter from user prefs: (old) filter type "
                                + filter.javaClass.getName()
                                + " is not assignable to (new) filter type "
                                + filterClazz.javaClass.getName()
                                + " (OK, probably new software release).")
            }
        }
        val result = filterClazz.newInstance()
        result.reset()
        userXmlPreferencesService.putEntry(session, filterClazz.name + ":Filter", result, true)
        return result
    }
}
