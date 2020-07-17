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

package org.projectforge.plugins.travel

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.BooleanSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.convertValue
import org.apache.commons.lang3.StringUtils
import org.projectforge.business.user.UserPrefDao
import org.projectforge.framework.json.*
import org.projectforge.framework.persistence.api.BaseDao
import org.projectforge.framework.persistence.api.BaseSearchFilter
import org.projectforge.framework.persistence.api.QueryFilter
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.framework.time.PFDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.io.IOException
import java.time.LocalDate

/**
 * @author Jan Brümmer (j.bruemmer@micromata.de)
 */
@Repository
open class TravelCostDao protected constructor() : BaseDao<TravelCostDO>(TravelCostDO::class.java) {
    private val log = org.slf4j.LoggerFactory.getLogger(UserPrefDao::class.java)

    @Autowired
    private lateinit var travelCostSendMailService: TravelCostSendMailService

    init {
        userRightId = TravelPluginUserRightId.PLUGIN_TRAVEL
    }

    /**
     * Load only memo's of current logged-in user.
     *
     * @param filter
     * @return
     */
    override fun createQueryFilter(filter: BaseSearchFilter): QueryFilter {
        val queryFilter = super.createQueryFilter(filter)
        val user = PFUserDO()
        user.id = ThreadLocalUserContext.getUserId()
        queryFilter.add(QueryFilter.eq("user", user))
        return queryFilter
    }

    fun deserizalizeValueObject(travelCost: TravelCostDO): Any? {
        if (travelCost.valueType == null)
            return null
        val json = fromJson(travelCost.cateringValueString!!, travelCost.valueType)
        as HashSet<*>

        val result: HashSet<CateringDay> = HashSet()

        json.forEach {
            result.add(getObjectMapper().convertValue(it))
        }

        travelCost.cateringValueObject = result
        return travelCost.cateringValueObject
    }

    private fun isJsonObject(value: String): Boolean {
        return StringUtils.startsWith(value, MAGIC_JSON_START)
    }

    private fun <T> fromJson(json: String?, classOfT: Class<T>?): T? {
        var json = json ?: ""
        if (!isJsonObject(json))
            return null
        json = json.substring(MAGIC_JSON_START.length)
        try {
            return getObjectMapper().readValue(json, classOfT!!)
        } catch (ex: IOException) {
            log.error("Can't deserialize json object (may-be incompatible ProjectForge versions): " + ex.message + " json=" + json, ex)
            return null
        }

    }

    override fun onSaveOrModify(obj: TravelCostDO) {
        if (obj.cateringValueObject == null) {
            obj.cateringValueString = null
            obj.cateringValueTypeString = null
        } else {
            obj.cateringValueString = toJson(obj.cateringValueObject)
            obj.cateringValueTypeString = obj.cateringValueObject!!.javaClass.name
        }
    }

    private fun toJson(obj: Any?): String {
        try {
            return MAGIC_JSON_START + getObjectMapper().writeValueAsString(obj)
        } catch (ex: JsonProcessingException) {
            log.error("Error while trying to serialze object as json: " + ex.message, ex)
            return ""
        }
    }

    override fun afterSave(obj: TravelCostDO) {
        super.afterSave(obj)
        //travelCostSendMailService.checkAndSendMail(obj, OperationType.INSERT)
    }

    override fun newInstance(): TravelCostDO {
        return TravelCostDO()
    }

    companion object {
        private var objectMapper: ObjectMapper? = null

        private val MAGIC_JSON_START = "^JSON:"

        fun getObjectMapper(): ObjectMapper {
            if (objectMapper != null) {
                return objectMapper!!
            }
            val mapper = ObjectMapper()
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
            mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val module = SimpleModule()

            module.addSerializer(LocalDate::class.java, LocalDateSerializer())
            module.addDeserializer(LocalDate::class.java, LocalDateDeserializer())

            module.addSerializer(PFDateTime::class.java, PFDateTimeSerializer())
            module.addDeserializer(PFDateTime::class.java, PFDateTimeDeserializer())

            module.addSerializer(Boolean::class.java, BooleanSerializer(false))
            module.addDeserializer(Boolean::class.java, NumberDeserializers.BooleanDeserializer(Boolean::class.java, false))

            module.addSerializer(java.util.Date::class.java, UtilDateSerializer(UtilDateFormat.ISO_DATE_TIME_SECONDS))
            module.addDeserializer(java.util.Date::class.java, UtilDateDeserializer())

            module.addSerializer(java.sql.Date::class.java, SqlDateSerializer())
            module.addDeserializer(java.sql.Date::class.java, SqlDateDeserializer())

            mapper.registerModule(module)
            mapper.registerModule(KotlinModule())
            objectMapper = mapper
            return mapper
        }
    }




}
