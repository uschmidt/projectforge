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

package org.projectforge.rest.config

import mu.KotlinLogging
import org.projectforge.rest.MyAccountPageRest
import org.projectforge.rest.TokenInfoPageRest
import org.projectforge.rest.UserServicesRest
import org.projectforge.rest.core.RestResolver
import org.projectforge.security.My2FARequestHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

private val log = KotlinLogging.logger {}

/**
 * Definition of 2FA shortcuts
 */
@Configuration
open class ProjectForge2FAInitialization {
  @Autowired
  private lateinit var my2FARequestHandler: My2FARequestHandler

  @PostConstruct
  private fun init() {
    my2FARequestHandler.registerShortCut(
      "ADMIN",
      "WRITE:user;WRITE:group;/wa/userEdit;/wa/groupEdit;/wa/admin;" +
          "/rs/change.*Password;/wa/license;/wa/access;/rs/adminLogViewer;" +
          // LuceneConsole, GroovyConsole, SQLConsole:
          "/wa/wicket/bookmarkable/org.projectforge.web.admin;" +
          "/wa/configuration"
    )
    my2FARequestHandler.registerShortCut(
      "HR",
      "WRITE:employee;/wa/employee;/wa/wicket/bookmarkable/org.projectforge.plugins.eed"
    )
    my2FARequestHandler.registerShortCut(
      "FINANCE",
      "WRITE:incomingInvoice;WRITE:outgoingInvoice;/wa/report;/wa/accounting;/wa/datev;/wa/liquidity;/react/account;/react/cost1;/react/cost2;/wa/incomingInvoice;/wa/outgoingInvoice"
    )
    my2FARequestHandler.registerShortCut(
      "ORGA",
      "WRITE:incomingMail;WRITE:outgoingMail;WRITE:contract;/wa/incomingMail;/react/outgoingMail;/wa/outgoingMail;/react/incomingMail;/wa/contractMail;/react/contract"
    )
    my2FARequestHandler.registerShortCut(
      "SCRIPT", "/react/script"
    )
    my2FARequestHandler.registerShortCut(
      "MY_ACCOUNT",
      "${basePath(MyAccountPageRest::class.java)};${basePath(TokenInfoPageRest::class.java)};${basePath(UserServicesRest::class.java, "renewToken")}"
    )
    my2FARequestHandler.registerShortCut("PASSWORD", "/react/change.*Password")
    log.info(my2FARequestHandler.printConfiguration())
  }

  private fun basePath(clazz: Class<*>, subPath: String? = null): String {
return     RestResolver.getRestUrl(clazz, subPath)
  }
}
