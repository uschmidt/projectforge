package org.projectforge.rest

import de.micromata.genome.db.jpa.xmldump.api.JpaXmlDumpService
import mu.KotlinLogging
import org.projectforge.ProjectForgeVersion
import org.projectforge.SystemAlertMessage
import org.projectforge.business.book.BookDao
import org.projectforge.business.meb.MebMailClient
import org.projectforge.business.systeminfo.SystemService
import org.projectforge.business.user.UserXmlPreferencesCache
import org.projectforge.business.user.UserXmlPreferencesMigrationDao
import org.projectforge.framework.access.AccessChecker
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.i18n.translateMsg
import org.projectforge.framework.persistence.api.ReindexSettings
import org.projectforge.framework.persistence.database.DatabaseService
import org.projectforge.framework.persistence.history.HibernateSearchReindexer
import org.projectforge.framework.persistence.jpa.PfEmgrFactory
import org.projectforge.plugins.core.PluginAdminService
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDynamicPageRest
import org.projectforge.rest.core.RestResolver
import org.projectforge.rest.dto.FormLayoutData
import org.projectforge.rest.dto.PostData
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("${Rest.URL}/admin")
class AdminPageRest : AbstractDynamicPageRest() {
    internal val NUMBER_OF_TEST_OBJECTS_TO_CREATE = 100

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
    internal var pluginAdminService: PluginAdminService? = null

    @Autowired
    @Transient
    protected var accessChecker: AccessChecker? = null

    class AdminData(
            val alertMessage: String? = SystemAlertMessage.alertMessage,
            val reindexFromDate: LocalDate? = null
    )


    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData {
        val data = AdminData()
        val alertMessage = UITextArea("alertMessage",
                label = translate("system.admin.group.title.alertMessage"),
                maxLength = 1000)
        val copyPasteText = UILabel(translate("system.admin.alertMessage.copyAndPaste.title") + " : " + translateMsg("system.admin.alertMessage.copyAndPaste.text", ProjectForgeVersion.VERSION_NUMBER))
        val newestEntries = UIInput("newestEntries",
                label = translate("system.admin.reindex.newestEntries"),
                tooltip = translate("system.admin.reindex.newestEntries.tooltip"))
        val reindexDate = UIInput("reindexDate",
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

        return FormLayoutData(data, layout, createServerData(request))
    }

    @PostMapping("alertMessage")
    fun setAlertMessage(request: HttpServletRequest, @RequestBody postData: PostData<AdminData>) {
        log.info("Admin user has set the alert message: \"" + postData.data.alertMessage + "\"")
        checkAccess()
        SystemAlertMessage.alertMessage = postData.data.alertMessage
    }

    @PostMapping("reindex")
    fun reindex(request: HttpServletRequest, @RequestBody postData: PostData<AdminData>) {
        log.info("Administration: re-index.")
        checkAccess()
        val settings = ReindexSettings(postData.data.reindexFromDate, postData.data.reindexNewestNEntries)
        SystemAlertMessage.alertMessage = postData.data.alertMessage
    }

    private fun checkAccess() {
        accessChecker!!.checkIsLoggedInUserMemberOfAdminGroup()
        accessChecker!!.checkRestrictedOrDemoUser()
    }
}