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

package org.projectforge.rest

import de.micromata.genome.db.jpa.xmldump.api.JpaXmlDumpService
import de.micromata.genome.util.runtime.RuntimeIOException
import mu.KotlinLogging
import org.apache.commons.collections.CollectionUtils
import org.projectforge.ProjectForgeVersion
import org.projectforge.SystemAlertMessage
import org.projectforge.SystemStatus
import org.projectforge.business.book.BookDO
import org.projectforge.business.book.BookDao
import org.projectforge.business.book.BookStatus
import org.projectforge.business.meb.MebMailClient
import org.projectforge.business.systeminfo.SystemService
import org.projectforge.business.tasktree.TaskTreeHelper
import org.projectforge.business.user.UserXmlPreferencesCache
import org.projectforge.business.user.UserXmlPreferencesMigrationDao
import org.projectforge.framework.access.AccessChecker
import org.projectforge.framework.configuration.ConfigXml
import org.projectforge.framework.configuration.Configuration
import org.projectforge.framework.configuration.GlobalConfiguration
import org.projectforge.framework.i18n.I18nHelper
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.i18n.translateMsg
import org.projectforge.framework.persistence.api.ReindexSettings
import org.projectforge.framework.persistence.database.DatabaseService
import org.projectforge.framework.persistence.history.HibernateSearchReindexer
import org.projectforge.framework.persistence.jpa.PfEmgrFactory
import org.projectforge.framework.time.DateHelper
import org.projectforge.framework.time.PFDateTime
import org.projectforge.menu.MenuItem
import org.projectforge.menu.MenuItemTargetType
import org.projectforge.plugins.core.PluginAdminService
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDynamicPageRest
import org.projectforge.rest.core.RestResolver
import org.projectforge.rest.dto.FormLayoutData
import org.projectforge.rest.dto.PostData
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDate
import java.util.*
import java.util.zip.GZIPOutputStream
import javax.servlet.http.HttpServletRequest

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("${Rest.URL}/admin")
class AdminPageRest : AbstractDynamicPageRest() {
    private val NUMBER_OF_TEST_OBJECTS_TO_CREATE = 100

    @Autowired
    private val bookDao: BookDao? = null

    @Autowired
    private val jpaXmlDumpService: JpaXmlDumpService? = null

    @Autowired
    private val systemService: SystemService? = null

    @Autowired
    private val databaseService: DatabaseService? = null

    @Autowired
    private val hibernateSearchReindexer: HibernateSearchReindexer? = null

    @Autowired
    private val mebMailClient: MebMailClient? = null

    @Autowired
    private val userXmlPreferencesCache: UserXmlPreferencesCache? = null

    @Autowired
    private val userXmlPreferencesMigrationDao: UserXmlPreferencesMigrationDao? = null

    @Autowired
    private val emf: PfEmgrFactory? = null

    @Autowired
    @Transient
    private var accessChecker: AccessChecker? = null

    class AdminData(
            val alertMessage: String? = SystemAlertMessage.alertMessage,
            val reindexFromDate: LocalDate? = null,
            val reindexNewestNEntries: Int = 1000
    )


    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData {
        val data = AdminData()
        var menuIndex = 0
        val alertMessage = UITextArea("alertMessage",
                label = translate("system.admin.group.title.alertMessage"),
                maxLength = 1000)
        val copyPasteText = UILabel(translate("system.admin.alertMessage.copyAndPaste.title") + " : " + translateMsg("system.admin.alertMessage.copyAndPaste.text", ProjectForgeVersion.VERSION_NUMBER))
        val newestEntries = UIInput("reindexNewestNEntries",
                label = translate("system.admin.reindex.newestEntries"),
                tooltip = translate("system.admin.reindex.newestEntries.tooltip"))
        val reindexDate = UIInput("reindexFromDate",
                label = translate("system.admin.reindex.fromDate"),
                tooltip = translate("system.admin.reindex.fromDate.tooltip"),
                dataType = UIDataType.DATE)

        val layout = UILayout("system.admin.title")
                .add(UIRow()
                        .add(UICol()
                                .add(alertMessage)
                                .add(copyPasteText)
                        )
                        .add(UICol()
                                .add(newestEntries)
                                .add(reindexDate))
                )
                .addAction(UIButton("setAlertMessage",
                        title = translate("system.admin.button.setAlertMessage"),
                        tooltip = translate("system.admin.button.setAlertMessage.tooltip"),
                        responseAction = ResponseAction(RestResolver.getRestUrl(this::class.java) + "/alertMessage", targetType = TargetType.POST)
                ))
                .addAction(UIButton("reindex",
                        title = translate("system.admin.button.reindex"),
                        tooltip = translate("system.admin.button.reindex.tooltip"),
                        responseAction = ResponseAction(RestResolver.getRestUrl(this::class.java) + "/reindex", targetType = TargetType.POST)
                ))

        val databaseActionsMenu = MenuItem("admin.databaseActions", i18nKey = "system.admin.group.title.databaseActions")

        databaseActionsMenu.add(MenuItem("admin.updateUserPrefs",
                i18nKey = "system.admin.button.updateUserPrefs",
                url = RestResolver.getRestUrl(this::class.java) + "/updateUserPrefs",
                tooltip = "system.admin.button.updateUserPrefs.tooltip",
                type = MenuItemTargetType.DOWNLOAD))

        databaseActionsMenu.add(MenuItem("admin.createMissingDatabaseIndices",
                i18nKey = "system.admin.button.createMissingDatabaseIndices",
                url = RestResolver.getRestUrl(this::class.java) + "/createMissingDatabaseIndices",
                tooltip = "system.admin.button.createMissingDatabaseIndices.tooltip",
                type = MenuItemTargetType.RESTCALL))

        // TODO: ConfirmMessage
        databaseActionsMenu.add(MenuItem("admin.dump",
                i18nKey = "system.admin.button.dump",
                url = RestResolver.getRestUrl(this::class.java) + "/dump",
                tooltip = "system.admin.button.dump.tooltip",
                type = MenuItemTargetType.DOWNLOAD))

        databaseActionsMenu.add(MenuItem("admin.schemaExport",
                i18nKey = "system.admin.button.schemaExport",
                url = RestResolver.getRestUrl(this::class.java) + "/schemaExport",
                tooltip = "system.admin.button.schemaExport.tooltip",
                type = MenuItemTargetType.DOWNLOAD))

        layout.add(databaseActionsMenu, menuIndex++)

        val cachesMenu = MenuItem("admin.caches", i18nKey = "system.admin.group.title.systemChecksAndFunctionality.caches")

        cachesMenu.add(MenuItem("admin.refreshCaches",
                i18nKey = "system.admin.button.refreshCaches",
                url = RestResolver.getRestUrl(this::class.java) + "/refreshCaches",
                tooltip = "system.admin.button.refreshCaches.tooltip",
                type = MenuItemTargetType.DOWNLOAD))

        layout.add(cachesMenu, menuIndex++)

        val configurationMenu = MenuItem("admin.configuration", i18nKey = "system.admin.group.title.systemChecksAndFunctionality.configuration")

        configurationMenu.add(MenuItem("admin.rereadConfiguration",
                i18nKey = "system.admin.button.rereadConfiguration",
                url = RestResolver.getRestUrl(this::class.java) + "/rereadConfiguration",
                tooltip = "system.admin.button.rereadConfiguration.tooltip",
                type = MenuItemTargetType.RESTCALL))

        configurationMenu.add(MenuItem("admin.exportConfiguration",
                i18nKey = "system.admin.button.exportConfiguration",
                url = RestResolver.getRestUrl(this::class.java) + "/exportConfiguration",
                tooltip = "system.admin.button.exportConfiguration.tooltip",
                type = MenuItemTargetType.DOWNLOAD))

        layout.add(configurationMenu, menuIndex++)

        val miscMenu = MenuItem("admin.miscChecks", i18nKey = "system.admin.group.title.systemChecksAndFunctionality.miscChecks")

        miscMenu.add(MenuItem("admin.checkSystemIntegrity",
                i18nKey = "system.admin.button.checkSystemIntegrity",
                url = RestResolver.getRestUrl(this::class.java) + "/checkSystemIntegrity",
                tooltip = "system.admin.button.checkSystemIntegrity.tooltip",
                type = MenuItemTargetType.DOWNLOAD))

        layout.add(miscMenu, menuIndex++)

        if (Configuration.getInstance().isMebConfigured) {
            val mebMenu = MenuItem("admin.databaseActions", i18nKey = "meb.title.heading")

            mebMenu.add(MenuItem("admin.checkUnseenMebMails",
                    i18nKey = "system.admin.button.checkUnseenMebMails",
                    url = RestResolver.getRestUrl(this::class.java) + "/checkUnseenMebMails",
                    tooltip = "system.admin.button.checkUnseenMebMails.tooltip",
                    type = MenuItemTargetType.RESTCALL))

            mebMenu.add(MenuItem("admin.importAllMebMails",
                    i18nKey = "system.admin.button.importAllMebMails",
                    url = RestResolver.getRestUrl(this::class.java) + "/importAllMebMails",
                    tooltip = "system.admin.button.importAllMebMails.tooltip",
                    type = MenuItemTargetType.RESTCALL))

            layout.add(mebMenu, menuIndex++)
        }

        if(SystemStatus.isDevelopmentMode()){
            val developmentMenu = MenuItem("admin.development", i18nKey = "")

            developmentMenu.add(MenuItem("admin.checkI18nProperties",
                    i18nKey = "system.admin.button.checkI18nProperties",
                    url = RestResolver.getRestUrl(this::class.java) + "/checkI18nProperties",
                    tooltip = "system.admin.button.checkI18nProperties.tooltip",
                    type = MenuItemTargetType.DOWNLOAD))

            developmentMenu.add(MenuItem("admin.createTestBooks",
                    i18nKey = "system.admin.button.createTestBooks",
                    url = RestResolver.getRestUrl(this::class.java) + "/createTestBooks",
                    tooltip = "Creates 100 books of type BookDO for testing.",
                    type = MenuItemTargetType.RESTCALL))

            layout.add(developmentMenu, menuIndex)
        }

        return FormLayoutData(data, layout, createServerData(request))
    }

    @PostMapping("alertMessage")
    fun setAlertMessage(request: HttpServletRequest, @RequestBody postData: PostData<AdminData>) {
        log.info("Admin user has set the alert message: \"" + postData.data.alertMessage + "\"")
        checkAccess()
        SystemAlertMessage.alertMessage = postData.data.alertMessage
    }

    @PostMapping("reindex")
    fun reindex(request: HttpServletRequest, @RequestBody postData: PostData<AdminData>): ResponseEntity<ResponseAction> {
        log.info("Administration: re-index.")
        checkAccess()
        val fromDate = PFDateTime.from(postData.data.reindexFromDate!!).sqlDate
        val settings = ReindexSettings(fromDate, postData.data.reindexNewestNEntries)
        val tables = hibernateSearchReindexer!!.rebuildDatabaseSearchIndices(settings)
        return ResponseEntity(ResponseAction("/dynamic",
                message = ResponseAction.Message("administration.databaseSearchIndicesRebuild", tables),
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    @GetMapping("updateUserPrefs")
    fun updateUserPrefs(): ResponseEntity<Any> {
        checkAccess()
        log.info("Administration: updateUserPrefs")
        val output = userXmlPreferencesMigrationDao!!.migrateAllUserPrefs()
        val content = output.toByteArray()
        val ts = DateHelper.getTimestampAsFilenameSuffix(Date())
        val filename = "projectforge_updateUserPrefs_$ts.txt"
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(content)
    }

    @GetMapping("createMissingDatabaseIndices")
    fun createMissingDatabaseIndices(): ResponseEntity<ResponseAction>{
        log.info("Administration: create missing data base indices.")
        accessChecker!!.checkRestrictedOrDemoUser()
        val counter = databaseService!!.createMissingIndices()
        return ResponseEntity(ResponseAction("/dynamic",
                message = ResponseAction.Message("administration.missingDatabaseIndicesCreated", messageParams = *arrayOf(counter.toString())),
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    @GetMapping("dump")
    fun dump(): ResponseEntity<Any> {
        log.info("Administration: Database dump.")
        checkAccess()
        val ts = DateHelper.getTimestampAsFilenameSuffix(Date())
        val filename = "projectforgedump_$ts.xml.gz"
        val out = ByteArrayOutputStream()
        try {
            GZIPOutputStream(out).use { gzout -> jpaXmlDumpService!!.dumpToXml(emf, gzout) }
        } catch (ex: IOException) {
            throw RuntimeIOException(ex)
        }

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(out.toByteArray())
    }

    @GetMapping("schemaExport")
    fun schemaExport(): ResponseEntity<Any> {
        log.info("Administration: schema export.")
        checkAccess()
        val result = systemService!!.exportSchema()
        val filename = "projectforge_schema" + DateHelper.getDateAsFilenameSuffix(Date()) + ".sql"

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(result.toByteArray())
    }

    @GetMapping("checkSystemIntegrity")
    fun checkSystemIntegrity(): ResponseEntity<Any> {
        log.info("Administration: check integrity of tasks.")
        checkAccess()
        val result = systemService!!.checkSystemIntegrity()
        val filename = "projectforge_check_report" + DateHelper.getDateAsFilenameSuffix(Date()) + ".txt"

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(result.toByteArray())
    }

    @GetMapping("refreshCaches")
    fun refreshCaches(): ResponseEntity<ResponseAction> {
        log.info("Administration: refresh of caches.")
        checkAccess()
        var refreshedCaches = systemService!!.refreshCaches()
        userXmlPreferencesCache!!.forceReload()
        refreshedCaches += ", UserXmlPreferencesCache"
        return ResponseEntity(ResponseAction("/dynamic",
                message = ResponseAction.Message("administration.refreshCachesDone", messageParams = *arrayOf(refreshedCaches)),
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    @GetMapping("rereadConfiguration")
    fun rereadConfiguration(): ResponseEntity<ResponseAction> {
        log.info("Administration: Reload all configurations (DB, XML)")
        checkAccess()
        log.info("Administration: reload global configuration.")
        GlobalConfiguration.getInstance().forceReload()
        log.info("Administration: reload configuration.")
        Configuration.getInstance().forceReload()
        log.info("Administration: reread configuration file config.xml.")
        var result: String? = ConfigXml.getInstance().readConfiguration()
        if (result != null) {
            result = result.replace("\n".toRegex(), "<br/>\n")
        }
        return ResponseEntity(ResponseAction("/dynamic",
                message = ResponseAction.Message("administration.rereadConfiguration", messageParams = *arrayOf(result!!)),
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    @GetMapping("exportConfiguration")
    fun exportConfiguration(): ResponseEntity<Any> {
        log.info("Administration: export configuration file config.xml.")
        checkAccess()
        val xml = ConfigXml.getInstance().exportConfiguration()
        val filename = "config-" + DateHelper.getDateAsFilenameSuffix(Date()) + ".xml"

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(xml.toByteArray())
    }

    @GetMapping("checkUnseenMebMails")
    fun checkUnseenMebMails(): ResponseEntity<ResponseAction>{
        log.info("Administration: check for new MEB mails.")
        checkAccess()
        val counter = mebMailClient!!.getNewMessages(true, true)
        return ResponseEntity(ResponseAction("/dynamic",
                message = ResponseAction.Message("message.successfullCompleted", messageParams = *arrayOf("check for new MEB mails, $counter new messages imported.")),
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    @GetMapping("importAllMebMails")
    fun importAllMebMails(): ResponseEntity<ResponseAction>{
        log.info("Administration: import all MEB mails.")
        checkAccess()
        val counter = mebMailClient!!.getNewMessages(false, false)
        return ResponseEntity(ResponseAction("/dynamic",
                message = ResponseAction.Message("message.successfullCompleted", messageParams = *arrayOf("import all MEB mails, $counter new messages imported.")),
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    @GetMapping("checkI18nProperties")
    fun checkI18nProperties(): ResponseEntity<Any> {
        log.info("Administration: check i18n properties.")
        checkAccess()
        val buf = StringBuilder()
        val warnMessages = StringBuilder()
        val propsFound = Properties()
        try {
            val cLoader = this.javaClass.classLoader
            val `is` = cLoader.getResourceAsStream("i18nKeys.properties")
            propsFound.load(`is`)
        } catch (ex: IOException) {
            log.error("Could not load i18n properties: " + ex.message, ex)
            throw RuntimeException(ex)
        }

        val defaultMap = load(warnMessages, "")
        val deMap = load(warnMessages, "_de")
        buf.append("Checking the differences between the i18n resource properties (default and _de)\n\n")
        buf.append("Found " + defaultMap.size + " entries in default property file (en).\n\n")
        buf.append("Missing in _de:\n")
        buf.append("---------------\n")
        for (key in defaultMap.keys) {
            if (!deMap.containsKey(key)) {
                buf.append(key).append("=").append(defaultMap[key]).append("\n")
            }
        }
        buf.append("\n\nOnly in _de (not in _en):\n")
        buf.append("-------------------------\n")
        for (key in deMap.keys) {
            if (!defaultMap.containsKey(key)) {
                buf.append(key).append("=").append(deMap[key]).append("\n")
            }
        }
        buf.append("\n\nWarnings and errors:\n")
        buf.append("--------------------\n")
        buf.append(warnMessages)
        buf.append("\n\nMaybe not defined but used (found in java, jsp or Wicket's html code):\n")
        buf.append("----------------------------------------------------------------------\n")
        for (key in propsFound.keys) {
            if (!defaultMap.containsKey(key) && !deMap.containsKey(key)) {
                buf.append(key).append("=").append(propsFound.getProperty(key as String)).append("\n")
            }
        }
        buf.append("\n\nExperimental (in progress): Maybe unused (not found in java, jsp or Wicket's html code):\n")
        buf.append("----------------------------------------------------------------------------------------\n")
        val all = TreeSet<String>()
        CollectionUtils.addAll(all, defaultMap.keys.iterator())
        CollectionUtils.addAll(all, deMap.keys.iterator())
        for (key in all) {
            if (propsFound.containsKey(key)) {
                continue
            }
            var value: String? = defaultMap[key]
            if (value == null) {
                value = deMap[key]
            }
            buf.append("$key=$value\n")
        }
        val result = buf.toString()
        val filename = "projectforge_i18n_check" + DateHelper.getDateAsFilenameSuffix(Date()) + ".txt"

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(result.toByteArray())
    }

    private fun load(warnMessages: StringBuilder, locale: String): SortedMap<String, String> {
        val cLoader = this.javaClass.classLoader
        val map = TreeMap<String, String>()
        val resourceBundleList = getResourceBundleNames()
        for (bundle in resourceBundleList) {
            val path = bundle.replace('.', '/') + locale + ".properties"
            log.info("Loading i18 resource properties: $path")
            val `is` = cLoader.getResourceAsStream(path)
            val properties = Properties()
            if (`is` != null) {
                try {
                    properties.load(`is`)
                } catch (ex: IOException) {
                    log.error("Error while loading resource properties '" + path + locale + ".properties: " + ex.message,
                            ex)
                    continue
                }

            }
            for (key in properties.keys) {
                val value = properties.getProperty(key as String)
                if (map.containsKey(key)) {
                    warnMessages.append("Duplicate entry (locale=").append(locale).append("): ").append(key)
                }
                map[key] = value
                if (value != null && (value.contains("{0") || value.contains("{1")) && value.contains("'")) {
                    // Message, check for single quotes:
                    var lastChar = ' '
                    for (element in value) {
                        if (lastChar == '\'') {
                            if (element != '\'') {
                                warnMessages.append("Key '").append(key).append("' (locale=").append(locale)
                                        .append(
                                                ") contains invalid message string (single quotes are not allowed and must be replaced by '').\n")
                                break
                            }
                            lastChar = ' ' // Quotes were OK.
                        } else {
                            lastChar = element
                        }
                    }
                }
            }
            log.info("Found " + map.size + " entries in: " + path)
        }
        return map
    }

    private fun getResourceBundleNames(): List<String> {
        val list = ArrayList<String>()
        list.addAll(I18nHelper.bundleNames)
        return list
    }

    @GetMapping("createTestBooks")
    fun createTestBooks(): ResponseEntity<ResponseAction> {
        accessChecker!!.checkIsLoggedInUserMemberOfAdminGroup()
        accessChecker!!.checkRestrictedOrDemoUser()
        val taskTree = TaskTreeHelper.getTaskTree()
        val list = ArrayList<BookDO>()
        var number = 1
        while (databaseService!!.queryForInt("select count(*) from t_book where title like 'title.$number.%'") > 0) {
            number++
        }
        for (i in 1..NUMBER_OF_TEST_OBJECTS_TO_CREATE) {
            val book = BookDO()
            book.title = get("title", number, i)
            book.abstractText = get("abstractText", number, i)
            book.authors = get("authors", number, i)
            book.comment = get("comment", number, i)
            book.editor = get("editor", number, i)
            book.isbn = get("isbn", number, i)
            book.keywords = get("keywords", number, i)
            book.publisher = get("publisher", number, i)
            book.signature = get("signature", number, i)
            book.status = BookStatus.PRESENT
            book.yearOfPublishing = "2001"
            list.add(book)
        }
        bookDao!!.save(list)
        return ResponseEntity(ResponseAction("/dynamic",
                message = ResponseAction.Message("system.admin.development.testObjectsCreated", "$NUMBER_OF_TEST_OBJECTS_TO_CREATE BookDO"),
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    private operator fun get(basename: String, number: Int, counter: Int): String {
        return "$basename.$number.$counter"
    }

    private fun checkAccess() {
        accessChecker!!.checkIsLoggedInUserMemberOfAdminGroup()
        accessChecker!!.checkRestrictedOrDemoUser()
    }
}
