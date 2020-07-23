package org.projectforge.plugins.travel

import mu.KotlinLogging
import org.projectforge.business.configuration.ConfigurationService
import org.projectforge.business.configuration.ConfigurationServiceAccessor
import org.projectforge.business.fibu.EmployeeDao
import org.projectforge.framework.access.OperationType
import org.projectforge.framework.configuration.ApplicationContextProvider
import org.projectforge.framework.i18n.I18nHelper
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.framework.time.DateTimeFormatter
import org.projectforge.mail.Mail
import org.projectforge.mail.SendMail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

private val log = KotlinLogging.logger {}

@Service
// TODO: Change accordingly to the needs for travel costs
class TravelCostSendMailService {
    @Autowired
    private lateinit var configurationService: ConfigurationService

    @Autowired
    private lateinit var employeeDao: EmployeeDao

    @Autowired
    private lateinit var sendMail: SendMail

    fun checkAndSendMail(obj: TravelCostDO, operationType: OperationType, dbObj: TravelCostDO? = null) {
        if (!configurationService.isSendMailConfigured) {
            log.info { "Mail server is not configured. No e-mail notification is sent." }
            return
        }
        val travelCostInfo = TravelCostInfo(sendMail, employeeDao, obj)
        if (!travelCostInfo.valid) {
            return
        }
        val traveller = travelCostInfo.employeeUser!!
        if (traveller.id != ThreadLocalUserContext.getUserId()) {
            sendMail(travelCostInfo, operationType, traveller, dbObj)
        }
    }

    private fun sendMail(travelCostInfo: TravelCostInfo, operationType: OperationType, recipient: PFUserDO?, dbObj: TravelCostDO? = null,
                         mailTo: String? = null) {
        val mail = prepareMail(travelCostInfo, operationType, recipient, dbObj, mailTo) ?: return
        sendMail.send(mail)
    }

    /**
     * Especially for testing.
     *
     * Analyzes the changes of the given vacation. If necessary, e-mails will be send to the involved
     * employees (replacement and management).
     * @param obj The object to save.
     * @param dbObj The already existing object in the data base (if updated). For new objects dbObj is null.
     */
    private fun prepareMail(travelCostInfo: TravelCostInfo, operationType: OperationType, recipient: PFUserDO?, dbObj: TravelCostDO? = null,
                            mailTo: String? = null): Mail? {
        if (!travelCostInfo.valid) {
            return null
        }
        travelCostInfo.updateI18n(recipient)
        val vacationer = travelCostInfo.employeeUser!!
        val obj = travelCostInfo.travelCost
        val operation = translate(recipient, "plugins.travel.mail.modType.${operationType.name.toLowerCase()}")

        val i18nArgs = arrayOf(travelCostInfo.employeeFullname,
                travelCostInfo.periodText, operation)
        val subject = translate(recipient, "plugins.travel.mail.action.short", *i18nArgs)
        val mailInfo = MailInfo(subject, operation, "own")
        val mail = Mail()
        mail.subject = subject
        mail.contentType = Mail.CONTENTTYPE_HTML
        var vacationerAsCC = false
        if (recipient != null) {
            mail.setTo(recipient)
            if (vacationer.id != recipient.id) {
                vacationerAsCC = true
            }
        }
        if (!mailTo.isNullOrBlank()) {
            mail.setTo(mailTo)
            vacationerAsCC = true
        }
        if (vacationerAsCC) {
            mail.addCC(vacationer.email)
        }
        if (mail.to.isEmpty()) {
            log.error { "Oups, no recipient is given to prepare mail. No notification is done." }
            return null
        }
        val data = mutableMapOf("travelCostInfo" to travelCostInfo, "travelCost" to obj, "mailInfo" to mailInfo)
        mail.content = sendMail.renderGroovyTemplate(mail, "mail/travelCostMail.html", data, translate(recipient, "plugins.travel"), recipient)
        return mail
    }

    internal class TravelCostInfo(sendMail: SendMail, employeeDao: EmployeeDao, val travelCost: TravelCostDO) {
        val link = getLinkToTravelCostEntry(travelCost.id)
        val modifiedByUser = ThreadLocalUserContext.getUser()
        val modifiedByUserFullname = modifiedByUser.getFullname()
        val modifiedByUserMail = modifiedByUser.email
        val employeeUser = employeeDao.internalGetById(travelCost.employee?.id)?.user
        var employeeFullname = employeeUser?.getFullname() ?: "unknown"
        val employeeMail = employeeUser?.email
        val startDate = dateFormatter.getFormattedDate(travelCost.beginOfTravel)
        val endDate = dateFormatter.getFormattedDate(travelCost.endOfTravel)
        // TODO
        val periodText = I18nHelper.getLocalizedMessage("plugins.travel.mail.period", startDate, endDate)
        var valid: Boolean = true

        /**
         * E-Mail be be sent to recipients with different locales.
         */
        fun updateI18n(recipient: PFUserDO?) {
            employeeFullname = employeeUser?.getFullname() ?: translate(recipient, "unknown")
        }

        fun formatModifiedByUser(): String {
            return formatUserWithMail(this.modifiedByUserFullname, this.modifiedByUserMail)
        }

        fun formatEmployee(): String {
            return formatUserWithMail(this.employeeFullname, this.employeeMail)
        }

        fun formatUserWithMail(name: String, mail: String? = null): String {
            if (mail == null) {
                return name
            }

            return "<a href=\"mailto:${mail}\">${name}</a>"
        }
    }

    private class MailInfo(val subject: String, val operation: String, val mode: String)

    companion object {
        private var _linkToVacationEntry: String? = null
        private val linkToTravelCostEntry: String
            get() {
                if (_linkToVacationEntry == null) {
                    val sendMail = ApplicationContextProvider.getApplicationContext().getBean(SendMail::class.java)
                    _linkToVacationEntry = sendMail.buildUrl("$travelCostEditPagePath/")
                }
                return _linkToVacationEntry!!
            }

        fun getLinkToTravelCostEntry(id: String) : String {
            return "$linkToTravelCostEntry$id?returnToCaller=account"
        }
        fun getLinkToTravelCostEntry(id: Int) : String {
            return getLinkToTravelCostEntry(id.toString())
        }
        private val travelCostEditPagePath = "react/travelCost/edit"
        private val dateFormatter = DateTimeFormatter.instance()

        private var _defaultLocale: Locale? = null
        private val defaultLocale: Locale
            get() {
                if (_defaultLocale == null) {
                    _defaultLocale = ConfigurationServiceAccessor.get().defaultLocale ?: Locale.getDefault()
                }
                return _defaultLocale!!
            }

        private fun translate(recipient: PFUserDO?, i18nKey: String, vararg params: Any): String {
            val locale = recipient?.locale ?: defaultLocale
            return I18nHelper.getLocalizedMessage(locale, i18nKey, *params)
        }

        private fun translate(recipient: PFUserDO?, value: Boolean?): String {
            return translate(recipient, if (value == true) "yes" else "no")
        }
    }
}
