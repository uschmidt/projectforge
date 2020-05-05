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

package org.projectforge.framework.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mu.KotlinLogging
import org.hibernate.proxy.AbstractLazyInitializer
import org.projectforge.business.fibu.KundeDO
import org.projectforge.business.fibu.kost.Kost2DO
import org.projectforge.common.BeanHelper
import org.projectforge.framework.jcr.Attachment
import org.projectforge.framework.json.JacksonBaseConfiguration.Companion.registerAllowedUnknownProperties
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.framework.time.PFDateTime
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalTime

private val log = KotlinLogging.logger {}

/**
 * Base configuration of all Spring rest calls. Unknown properties not avoidable by the client might be registered through
 * [registerAllowedUnknownProperties]. For example PFUserDO.fullname is provided as service for the clients, but is
 * an unknown field by PFUserDO.
 */
open class JacksonBaseConfiguration {
    companion object {

        private val allowedUnknownProperties = mutableMapOf<Class<*>, MutableSet<String>>()

        private val allowedUnknownGlobalProperties = mutableSetOf<String>()

        private val globalPropertiesBlackList = mutableMapOf<Class<*>, MutableSet<String>>()

        private val registeredSerializers = mutableListOf<Pair<Class<Any>, JsonSerializer<Any>>>()

        private val registeredDeserializers = mutableListOf<Pair<Class<Any>, JsonDeserializer<Any>>>()

        private val registeredDelegatingDeserializers = mutableListOf<Class<*>>()

        /**
         * Plugins may register your own serializers on startup.
         */
        @JvmStatic
        fun register(cls: Class<Any>, serializer: JsonSerializer<Any>) {
            registeredSerializers.add(Pair(cls, serializer))
        }

        /**
         * Plugins may register your own deserializers on startup.
         */
        @JvmStatic
        fun register(cls: Class<Any>, deserializer: JsonDeserializer<Any>) {
            registeredDeserializers.add(Pair(cls, deserializer))
        }

        /**
         * Plugins may register your own deserializers on startup.
         */
        @JvmStatic
        fun registeredDelegatingDeserializer(vararg classes: Class<*>) {
            classes.forEach { cls ->
                registeredDelegatingDeserializers.add(cls)
            }
        }

        /**
         * Properties (field) sent by any client and unknown by the server will result in an exception and BAD_REQUEST.
         * In special cases you may add properties, which should be simply ignored.
         */
        @JvmStatic
        fun registerAllowedUnknownProperties(clazz: Class<*>, vararg properties: String) {
            synchronized(allowedUnknownProperties) {
                val set = allowedUnknownProperties[clazz]
                if (set == null) {
                    allowedUnknownProperties[clazz] = mutableSetOf(*properties)
                } else {
                    set.addAll(properties)
                }
            }
        }

        /**
         * Properties (field) sent by any client and unknown by the server will result in an exception and BAD_REQUEST.
         * In special cases you may add properties, which should be simply ignored.
         */
        @JvmStatic
        fun registerAllowedUnknownGlobalProperties(vararg properties: String) {
            synchronized(allowedUnknownGlobalProperties) {
                allowedUnknownGlobalProperties.addAll(properties)
            }
        }

        init {
            registerAllowedUnknownProperties(Attachment::class.java, "sizeHumanReadable", "createdFormatted", "lastUpdateFormatted")
            registerAllowedUnknownProperties(PFUserDO::class.java, "fullname")
            registerAllowedUnknownProperties(KundeDO::class.java, "id")
            registerAllowedUnknownProperties(Kost2DO::class.java, "nummernkreis", "teilbereich", "bereich", "endziffer", "formattedNumber")
        }
    }

    protected var objectMapper: ObjectMapper? = null
    protected var module: SimpleModule? = null

    protected fun getRegisteredDelegatingDeserializers(): MutableList<Class<*>> {
        return registeredDelegatingDeserializers
    }

    fun <T> addSerializer(cls: Class<T>, serializer: JsonSerializer<T>) {
        objectMapper() // Force initialization.
        module?.addSerializer(cls, serializer)
    }

    fun <T> addDeserializer(cls: Class<T>, deserializer: JsonDeserializer<T>) {
        objectMapper() // Force initialization.
        module?.addDeserializer(cls, deserializer)
    }

    open fun objectMapper(): ObjectMapper {
        objectMapper?.let { return it }
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val module = object : SimpleModule() {
            override fun setupModule(context: SetupContext) {
                super.setupModule(context)
                context.addDeserializationProblemHandler(object : DeserializationProblemHandler() {
                    override fun handleUnknownProperty(ctxt: DeserializationContext?, p: JsonParser?, deserializer: JsonDeserializer<*>?, beanOrClass: Any?, propertyName: String?): Boolean {
                        if (beanOrClass == null)
                            return false
                        val clazz = if (beanOrClass is Class<*>) beanOrClass else beanOrClass.javaClass
                        if (allowedUnknownGlobalProperties.contains(propertyName)) {
                            return BeanHelper.determineSetter(clazz, propertyName) == null // Don't ignore global properties if setter is available.
                        }
                        return allowedUnknownProperties[clazz]?.contains(propertyName) ?: false
                    }
                })
            }
        }
        /* module.setDeserializerModifier(object : BeanDeserializerModifier() {
             override fun modifyDeserializer(config: DeserializationConfig, beanDesc: BeanDescription, deserializer: JsonDeserializer<*>): JsonDeserializer<*>? {
                 registeredDelegatingDeserializers.forEach {
                     if (beanDesc.beanClass == it) {
                         return IdObjectDeserializer(deserializer, it)
                     }
                 }
                 return deserializer
             }
         })*/
        module.addSerializer(LocalDate::class.java, LocalDateSerializer())
        module.addDeserializer(LocalDate::class.java, LocalDateDeserializer())

        module.addSerializer(LocalTime::class.java, LocalTimeSerializer())
        module.addDeserializer(LocalTime::class.java, LocalTimeDeserializer())

        module.addSerializer(PFDateTime::class.java, PFDateTimeSerializer())
        module.addDeserializer(PFDateTime::class.java, PFDateTimeDeserializer())

        module.addSerializer(java.util.Date::class.java, UtilDateSerializer(UtilDateFormat.JS_DATE_TIME_MILLIS))
        module.addDeserializer(java.util.Date::class.java, UtilDateDeserializer())

        module.addSerializer(Timestamp::class.java, TimestampSerializer(UtilDateFormat.JS_DATE_TIME_MILLIS))
        module.addDeserializer(Timestamp::class.java, TimestampDeserializer())

        module.addSerializer(java.sql.Date::class.java, SqlDateSerializer())
        module.addDeserializer(java.sql.Date::class.java, SqlDateDeserializer())

        module.addSerializer(AbstractLazyInitializer::class.java, HibernateProxySerializer())

        registeredSerializers.forEach {
            module.addSerializer(it.first, it.second)
        }
        registeredDeserializers.forEach {
            module.addDeserializer(it.first, it.second)
        }
        mapper.registerModule(module)
        this.objectMapper = mapper
        this.module = module
        return mapper
    }
}
