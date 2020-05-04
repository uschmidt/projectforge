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

package org.projectforge.database

import org.junit.jupiter.api.Test
import org.projectforge.business.fibu.AuftragDO
import org.projectforge.business.fibu.AuftragDao
import org.projectforge.business.fibu.AuftragsPositionDO
import org.projectforge.business.fibu.AuftragsPositionsStatus
import org.projectforge.framework.persistence.history.entities.PfHistoryAttrDO
import org.projectforge.framework.persistence.history.entities.PfHistoryAttrDataDO
import org.projectforge.framework.persistence.history.entities.PfHistoryMasterDO
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.test.AbstractTestBase
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.persistence.metamodel.EntityType

class DatabaseExportTest : AbstractTestBase() {
    @Autowired
    private lateinit var auftragDao: AuftragDao

    @Autowired
    private lateinit var databaseJsonExport: DatabaseJsonExport

    @Autowired
    private lateinit var databaseJsonImport: DatabaseJsonImport

    @Test
    fun exportTest() {
        val order = AuftragDO()
        order.nummer = auftragDao.nextNumber
        order.angebotsDatum = LocalDate.now()
        val pos = AuftragsPositionDO()
        pos.bemerkung = "comment"
        pos.nettoSumme = BigDecimal.TEN
        pos.status = AuftragsPositionsStatus.OPTIONAL
        order.addPosition(pos)
        auftragDao.internalSave(order)

        val file = File("target/test-export.zip")
        ZipOutputStream(FileOutputStream(file)).use { zipOut ->
            databaseJsonExport.exportEntity(file.name, zipOut, PFUserDO::class.java)
            databaseJsonExport.exportEntity(file.name, zipOut, AuftragDO::class.java)
        }
    }

    @Test
    fun exportAllTest() {
        val file = File("target/test-export-all.zip")
        ZipOutputStream(FileOutputStream(file)).use { zipOut ->
            databaseJsonExport.export(file.name, zipOut, PfHistoryAttrDO::class.java, PFUserDO::class.java)
        }

        ZipInputStream(FileInputStream(file)).use { zipIn ->
            databaseJsonImport.import(zipIn, object : DatabaseImporter() {
                override fun accept(entityType: EntityType<*>): Boolean {
                    log.info("Processing ${entityType.name}...")
                    if (entityType.javaType == PfHistoryAttrDataDO::class.java) {
                        return false
                    }
                    if (entityType.javaType == PfHistoryMasterDO::class.java) {
                        return false
                    }
                    return true
                }

                override fun import(list: List<out Any>, entityType: EntityType<*>) {
                    log.info("Importing ${list.size} items of type ${entityType.name}...")
                }
            })
        }
    }
}
