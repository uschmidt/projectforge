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

import mu.KotlinLogging
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpException
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.lang3.StringUtils
import org.projectforge.business.address.AddressDO
import org.projectforge.business.address.AddressDao
import org.projectforge.business.configuration.ConfigurationService
import org.projectforge.business.user.service.UserXmlPreferencesService
import org.projectforge.common.StringHelper
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.framework.time.DateTimeFormatter
import org.projectforge.framework.utils.NumberHelper
import org.projectforge.framework.utils.NumberHelper.extractPhonenumber
import org.projectforge.framework.utils.RecentQueue
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDynamicPageRest
import org.projectforge.rest.core.RestResolver
import org.projectforge.rest.dto.FormLayoutData
import org.projectforge.rest.dto.PostData
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletRequest

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("${Rest.URL}/phoneCall")
class PhoneCallPageRest : AbstractDynamicPageRest() {

    @Autowired
    private val addressDao: AddressDao? = null

    @Autowired
    private val configurationService: ConfigurationService? = null

    @Autowired
    private val userPreferencesService: UserXmlPreferencesService? = null

    protected var address: AddressDO? = null

    protected var phoneNumber: String? = null

    protected var myCurrentPhoneId: String? = null

    internal var result: String? = null

    internal var lastSuccessfulPhoneCall: Date? = null

    private var recentSearchTermsQueue: RecentQueue<String>? = null

    class PhoneCallData(
            var userId: Int? = null,
            var phoneNumber: String? = null,
            var myCurrentPhoneId: String? = null
    )

    @PostMapping
    internal fun call(request: HttpServletRequest, @RequestBody postData: PostData<PhoneCallData>) {
        this.phoneNumber = postData.data.phoneNumber
        this.myCurrentPhoneId = postData.data.myCurrentPhoneId
        val extracted = processPhoneNumber()
        if (extracted) {
            return
        }
        val number = extractPhonenumber(phoneNumber)
        if (number!!.isEmpty() || !StringUtils.containsOnly(number, "0123456789+-/() ")) {
            log.error("address.phoneCall.number.invalid")
            return
        }
        phoneNumber = extractPhonenumber(phoneNumber)
        callNow()
    }

    /**
     * For special phone numbers: id:# or # | name.
     *
     * @return true, if the phone number was successfully processed.
     */
    private fun processPhoneNumber(): Boolean {
        if (StringUtils.isNotEmpty(phoneNumber)) {
            if (phoneNumber!!.startsWith("id:") && phoneNumber!!.length > 3) {
                val id = NumberHelper.parseInteger(phoneNumber!!.substring(3))
                if (id != null) {
                    phoneNumber = ""
                    address = addressDao!!.getById(id)
                    if (address != null) {
                        val no = getFirstPhoneNumber()
                        if (StringUtils.isNotEmpty(no)) {
                            setPhoneNumber(no, true)
                        }
                    }
                }
                return true
            } else if (phoneNumber!!.indexOf(SEPARATOR) >= 0) {
                val pos = phoneNumber!!.indexOf(SEPARATOR)
                val rest = phoneNumber!!.substring(pos + SEPARATOR.length)
                val numberPos = rest.indexOf('#')
                phoneNumber = phoneNumber!!.substring(0, pos)
                if (numberPos > 0) {
                    val id = NumberHelper.parseInteger(rest.substring(numberPos + 1))
                    if (id != null) {
                        val address = addressDao!!.getById(id)
                        if (address != null) {
                            this.address = address
                        }
                    } else {
                        this.address = null
                    }
                } else {
                    this.address = null
                }
                return true
            }
        }
        return false
    }

    private fun callNow() {
        if (StringUtils.isBlank(configurationService!!.telephoneSystemUrl)) {
            log.error("Telephone system url not configured. Phone calls not supported.")
            return
        }
        log.info("User initiates direct call from phone with id '"
                + myCurrentPhoneId
                + "' to destination numer: "
                + StringHelper.hideStringEnding(phoneNumber, 'x', 3))
        result = null
        val buf = StringBuffer()
        buf.append(phoneNumber).append(SEPARATOR)
        if (address != null && StringHelper.isIn(phoneNumber, extractPhonenumber(address!!.businessPhone),
                        extractPhonenumber(address!!.mobilePhone), extractPhonenumber(address!!.privatePhone),
                        extractPhonenumber(address!!.privateMobilePhone))) {
            buf.append(address!!.firstName).append(" ").append(address!!.name)
            if (phoneNumber == extractPhonenumber(address!!.mobilePhone)) {
                buf.append(", ").append(translate("address.phoneType.mobile"))
            } else if (phoneNumber == extractPhonenumber(address!!.privatePhone)) {
                buf.append(", ").append(translate("address.phoneType.private"))
            }
            buf.append(" #").append(address!!.id)
        } else {
            buf.append("???")
        }
        val client = HttpClient()
        var url = configurationService.telephoneSystemUrl
        url = StringUtils.replaceOnce(url, "#source", myCurrentPhoneId)
        url = StringUtils.replaceOnce(url, "#target", phoneNumber)
        val urlProtected = StringHelper.hideStringEnding(url, 'x', 3)
        val method = GetMethod(url)
        var errorKey: String? = null
        try {
            lastSuccessfulPhoneCall = Date()
            client.executeMethod(method)
            val resultStatus = method.responseBodyAsString
            log.info("Call URL: $urlProtected with result code: $resultStatus")
            when (resultStatus) {
                "0" -> {
                    result = (DateTimeFormatter.instance().getFormattedDateTime(Date()) + ": "
                            + translate("address.phoneCall.result.successful"))
                    getRecentSearchTermsQueue().append(buf.toString())
                }
                "2" -> errorKey = "address.phoneCall.result.wrongSourceNumber"
                "3" -> errorKey = "address.phoneCall.result.wrongDestinationNumber"
                else -> errorKey = "address.phoneCall.result.callingError"
            }
        } catch (ex: HttpException) {
            result = "Call failed. Please contact administrator."
            log.error("$result: $urlProtected")
            throw RuntimeException(ex)
        } catch (ex: IOException) {
            result = "Call failed. Please contact administrator."
            log.error("$result: $urlProtected")
            throw RuntimeException(ex)
        }

        if (errorKey != null) {
            log.error(errorKey)
        }
    }

    /**
     * Find a phone number, search order is business, mobile, private mobile and private.
     *
     * @return Number if found, otherwise empty string.
     */
    protected fun getFirstPhoneNumber(): String? {
        if (address == null) {
            return ""
        }
        return when {
            StringUtils.isNotEmpty(address!!.businessPhone) -> address!!.businessPhone
            StringUtils.isNotEmpty(address!!.mobilePhone) -> address!!.mobilePhone
            StringUtils.isNotEmpty(address!!.privateMobilePhone) -> address!!.privateMobilePhone
            StringUtils.isNotEmpty(address!!.privatePhone) -> address!!.privatePhone
            else -> ""
        }
    }

    protected fun getRecentSearchTermsQueue(): RecentQueue<String> {
        if (recentSearchTermsQueue == null) {
            recentSearchTermsQueue = userPreferencesService!!.getEntry(USER_PREF_KEY_RECENTS) as RecentQueue<String>
        }
        if (recentSearchTermsQueue == null) {
            recentSearchTermsQueue = userPreferencesService!!.getEntry("org.projectforge.web.address.PhoneCallAction:recentSearchTerms") as RecentQueue<String>
            if (recentSearchTermsQueue != null) {
                // Old entries:
                userPreferencesService.putEntry(USER_PREF_KEY_RECENTS, recentSearchTermsQueue, true)
                userPreferencesService.removeEntry("org.projectforge.web.address.PhoneCallAction:recentSearchTerms")
            }
        }
        if (recentSearchTermsQueue == null) {
            recentSearchTermsQueue = RecentQueue()
            userPreferencesService!!.putEntry(USER_PREF_KEY_RECENTS, recentSearchTermsQueue, true)
        }
        return recentSearchTermsQueue!!
    }

    fun setPhoneNumber(number: String?, extract: Boolean) {
        phoneNumber = if (extract) {
            extractPhonenumber(number)
        } else {
            number
        }
    }

    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData {
        val user = ThreadLocalUserContext.getUser()
        val data = PhoneCallData(user.id)
        val phone = user.personalPhoneIdentifiers?.split(",")
        var select: UISelect<String> = UISelect("myCurrentPhoneId",
                label = "address.myCurrentPhoneId",
                tooltip = "address.myCurrentPhoneId.tooltip.content")

        if(phone != null){
            select = select.buildFromList(phone)
        } else {
            val selectValue: UISelectValue<String> = UISelectValue("pleaseDefine",
                    translate("user.personalPhoneIdentifiers.pleaseDefine"))
            select.values = mutableListOf(selectValue)
        }

        val layout = UILayout("address.phoneCall.title")

        val numberTextField = UIInput("phoneNumber",
                label = "address.phoneCall.number.label",
                additionalLabel = "address.phoneCall.number.labeldescription",
                tooltip = "address.directCall.number.tooltip",
                autoComplete = UIInput.AutoCompleteType.USERNAME)

        layout.add(numberTextField)
                .add(select)
                .addAction(UIButton("call",
                        translate("address.directCall.call"),
                        UIColor.SUCCESS,
                        responseAction = ResponseAction(RestResolver.getRestUrl(this::class.java), targetType = TargetType.POST),
                        default = true)
                )
        LayoutUtils.process(layout)
        return FormLayoutData(data, layout, createServerData(request))
    }

    companion object {
        private const val SEPARATOR = " | "

        private const val USER_PREF_KEY_RECENTS = "phoneCalls"
    }
}
