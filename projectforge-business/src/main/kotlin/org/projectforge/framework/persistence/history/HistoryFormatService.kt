/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2022 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.framework.persistence.history

import de.micromata.genome.db.jpa.history.api.HistoryEntry
import de.micromata.genome.db.jpa.history.entities.EntityOpType
import de.micromata.genome.db.jpa.history.entities.PropertyOpType
import mu.KotlinLogging
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.persistence.api.ExtendedBaseDO
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

private val log = KotlinLogging.logger {}

/**
 * History entries will be transformed into human readable formats.
 */
@Component
class HistoryFormatService {
  @PersistenceContext
  private lateinit var em: EntityManager

  @Autowired
  internal lateinit var applicationContext: ApplicationContext

  private val historyServiceAdapters =
    mutableMapOf<Class<out ExtendedBaseDO<Int>>, HistoryFormatAdapter>()

  private lateinit var stdHistoryFormatAdapter: HistoryFormatAdapter

  data class DisplayHistoryEntryDTO(
    var modifiedAt: Date? = null,
    var timeAgo: String? = null,
    var modifiedByUserId: String? = null,
    var modifiedByUser: String? = null,
    var operationType: EntityOpType? = null,
    var operation: String? = null,
    var diffEntries: MutableList<DisplayHistoryDiffEntryDTO> = mutableListOf()
  )

  data class DisplayHistoryDiffEntryDTO(
    var operationType: PropertyOpType? = null,
    var operation: String? = null,
    var property: String? = null,
    var oldValue: String? = null,
    var newValue: String? = null
  )

  @PostConstruct
  private fun postConstruct() {
    stdHistoryFormatAdapter = HistoryFormatAdapter(em, this)
    register(PFUserDO::class.java, HistoryFormatUserAdapter(em, this))
  }

  fun <O : ExtendedBaseDO<Int>> register(clazz: Class<out O>, historyServiceAdapter: HistoryFormatAdapter) {
    if (historyServiceAdapters[clazz] != null) {
      log.warn { "Can't register HistoryServiceAdapter ${historyServiceAdapter::class.java.name} twice. Ignoring." }
      return
    }
    this.historyServiceAdapters[clazz] = historyServiceAdapter
  }

  /**
   * Creates a list of formatted history entries (get the user names etc.)
   */
  fun <O : ExtendedBaseDO<Int>> format(item: O, orig: Array<HistoryEntry<*>>): List<DisplayHistoryEntryDTO> {
    val entries = mutableListOf<DisplayHistoryEntryDTO>()
    orig.forEach { historyEntry ->
      entries.add(convert(item, historyEntry))
    }
    val adapter = historyServiceAdapters[item::class.java]
    adapter?.convertEntries(item, entries)
    return entries.sortedByDescending { it.modifiedAt }
  }

  fun <O : ExtendedBaseDO<Int>> convert(item: O, historyEntry: HistoryEntry<*>): DisplayHistoryEntryDTO {
    val adapter = historyServiceAdapters[item::class.java]
    return adapter?.convert(item, historyEntry) ?: stdHistoryFormatAdapter.convert(item, historyEntry)
  }

  companion object {
    fun translate(opType: EntityOpType?): String {
      return when (opType) {
        EntityOpType.Insert -> translate("operation.inserted")
        EntityOpType.Update -> translate("operation.updated")
        EntityOpType.Deleted -> translate("operation.deleted")
        EntityOpType.MarkDeleted -> translate("operation.markAsDeleted")
        EntityOpType.UmarkDeleted -> translate("operation.undeleted")
        else -> ""
      }
    }

    fun translate(opType: PropertyOpType?): String {
      return when (opType) {
        PropertyOpType.Insert -> translate("operation.inserted")
        PropertyOpType.Update -> translate("operation.updated")
        PropertyOpType.Delete -> translate("operation.deleted")
        PropertyOpType.Undefined -> translate("operation.undefined")
        else -> ""
      }
    }
  }
}
