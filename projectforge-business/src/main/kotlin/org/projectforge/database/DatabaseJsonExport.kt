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

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.hibernate.Session
import org.projectforge.framework.json.JacksonBaseConfiguration
import org.projectforge.framework.persistence.user.entities.TenantDO
import org.springframework.stereotype.Service
import java.io.PrintWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

private val log = KotlinLogging.logger {}

/**
 * Exporting data base tables to json files.
 *
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Service
open class DatabaseJsonExport {
    @PersistenceContext
    private lateinit var em: EntityManager

    private val objectMapper: ObjectMapper

    init {
        val configuration = JacksonBaseConfiguration()
        objectMapper = configuration.objectMapper()
        configuration.addSerializer(TenantDO::class.java, TenantDOSerializer())
    }

    open fun export(archiveName: String, zipOut: ZipOutputStream, vararg skipEntities: Class<out Any>) {
        val entityManager = em.entityManagerFactory.createEntityManager()
        val sessionFactory = entityManager.unwrap(Session::class.java).sessionFactory
        val entities = sessionFactory.metamodel.entities
        entities.filter { !skipEntities.contains(it.javaType) }.forEach { entityClass ->
            exportEntity(archiveName, zipOut, entityClass.javaType)
        }
    }

    /**
     * Checks also the select access of the logged in user.
     */
    open fun exportEntity(archiveName: String, zipOut: ZipOutputStream, entity: Class<out Any>) {
        val archivNameWithoutExtension = if (archiveName.contains('.')) {
            archiveName.substring(0, archiveName.indexOf('.'))
        } else {
            archiveName
        }
        zipOut.putNextEntry(createZipEntry(archivNameWithoutExtension, "${entity.simpleName}.json"))
        val writer = PrintWriter(zipOut)
        export(writer, entity)
        writer.flush()
        zipOut.closeEntry()
    }

    /**
     * Checks also the select access of the logged in user.
     */
    open fun export(writer: PrintWriter, entity: Class<out Any>) {
        var first = true
        writer.println("[")
        val resultList = em.createQuery("SELECT a FROM ${entity.simpleName} a", entity).resultList
        log.info { "Exporting ${resultList.size} items of entity ${entity.simpleName}..." }
        resultList.forEach { obj ->
            if (first) {
                first = false
            } else {
                writer.println(",")
            }
            writer.println(objectMapper.writeValueAsString(obj))
        }
        writer.println()
        writer.print("]")
    }

    private fun createZipEntry(archiveName: String, vararg path: String?): ZipEntry {
        return ZipEntry("$archiveName/${path.joinToString(separator = "/") { it ?: "" }}")
    }

}
