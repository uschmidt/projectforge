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

import com.fasterxml.jackson.core.type.TypeReference
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.hibernate.Session
import org.hsqldb.types.Charset
import org.projectforge.framework.ToStringUtil
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

private val log = KotlinLogging.logger {}

/**
 * Importing data base tables from json files.
 *
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Service
open class DatabaseJsonImport {
    @PersistenceContext
    private lateinit var em: EntityManager

    open fun import(zipIn: ZipInputStream, importer: DatabaseImporter) {
        val entityManager = em.entityManagerFactory.createEntityManager()
        val sessionFactory = entityManager.unwrap(Session::class.java).sessionFactory
        val entities = sessionFactory.metamodel.entities

        var zipEntry = zipIn.nextEntry
        while (zipEntry != null) {
            if (zipEntry.isDirectory) {
                zipEntry = zipIn.nextEntry
                continue
            }
            val fileName = FilenameUtils.getBaseName(zipEntry.name)
            val entityType = entities.find { it.name == fileName } ?: continue
            if (importer.accept(entityType)) {
                val cls = entityType.javaType
               /* val mapper = JacksonCon.getObjectMapper(cls, null, null)
                val jsonArray = zipIn.readBytes().toString(StandardCharsets.UTF_8)
                val array = mapper.readerFor(cls).readValues<Any>(jsonArray)
                array.forEach {
                    println(ToStringUtil.toJsonString(it))
                }*/
            }
            zipEntry = zipIn.nextEntry
        }
        zipIn.closeEntry()
    }
}
