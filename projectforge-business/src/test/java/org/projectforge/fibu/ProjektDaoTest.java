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

package org.projectforge.fibu;

import org.junit.jupiter.api.Test;
import org.projectforge.business.fibu.ProjektDO;
import org.projectforge.business.fibu.ProjektDao;
import org.projectforge.business.fibu.ProjektFilter;
import org.projectforge.framework.access.AccessException;
import org.projectforge.framework.persistence.user.entities.GroupDO;
import org.projectforge.test.AbstractTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ProjektDaoTest extends AbstractTestBase {
  @Autowired
  private ProjektDao projektDao;

  @Test
  public void checkAccess() {
    logon(AbstractTestBase.TEST_FINANCE_USER);
    final GroupDO group = initTestDB.addGroup("ProjektDaoTest.ProjectManagers", AbstractTestBase.TEST_PROJECT_ASSISTANT_USER);
    ProjektDO projekt = new ProjektDO();
    projekt.setName("ACME - Webportal");
    projekt.setProjektManagerGroup(group);
    Serializable id = projektDao.save(projekt);
    projekt = projektDao.getById(id);
    projekt.setDescription("Test");
    projektDao.update(projekt);

    logon(AbstractTestBase.TEST_CONTROLLING_USER);
    checkNoWriteAccess(id, projekt, "Controlling");

    logon(AbstractTestBase.TEST_USER);
    checkNoAccess(id, "Other");
    checkNoAccess(id, projekt, "Other");

    logon(AbstractTestBase.TEST_PROJECT_MANAGER_USER);
    projektDao.getList(new ProjektFilter());
    checkNoAccess(id, projekt, "Project manager");

    logon(AbstractTestBase.TEST_PROJECT_ASSISTANT_USER);
    projektDao.getList(new ProjektFilter());
    checkNoWriteAccess(id, projekt, "Project assistant");
    checkNoHistoryAccess(id, projekt, "Project assistant");

    logon(AbstractTestBase.TEST_ADMIN_USER);
    checkNoAccess(id, projekt, "Admin ");
    checkNoAccess(id, projekt, "Project manager");
  }

  private void checkNoAccess(Serializable id, String who) {
    try {
      ProjektFilter filter = new ProjektFilter();
      projektDao.getList(filter);
      fail("AccessException expected: " + who + " users should not have select list access to projects.");
    } catch (AccessException ex) {
      // OK
    }
  }

  private void checkNoAccess(Serializable id, ProjektDO projekt, String who) {
    try {
      projektDao.getById(id);
      fail("AccessException expected: " + who + " users should not have select access to projects.");
    } catch (AccessException ex) {
      // OK
    }
    checkNoHistoryAccess(id, projekt, who);
    checkNoWriteAccess(id, projekt, who);
  }

  private void checkNoHistoryAccess(Serializable id, ProjektDO projekt, String who) {
    assertEquals(projektDao.hasLoggedInUserHistoryAccess(false), false,
            who + " users should not have select access to history of projects.");
    try {
      projektDao.hasLoggedInUserHistoryAccess(true);
      fail("AccessException expected: " + who + " users should not have select access to history of projects.");
    } catch (AccessException ex) {
      // OK
    }
    assertEquals(projektDao.hasLoggedInUserHistoryAccess(projekt, false), false,
            who + " users should not have select access to history of projects.");
    try {
      projektDao.hasLoggedInUserHistoryAccess(projekt, true);
      fail("AccessException expected: " + who + " users should not have select access to history of invoices.");
    } catch (AccessException ex) {
      // OK
    }
  }

  private void checkNoWriteAccess(Serializable id, ProjektDO projekt, String who) {
    try {
      ProjektDO ku = new ProjektDO();
      projekt.setName("ACME - Webportal 2");
      projektDao.save(ku);
      fail("AccessException expected: " + who + " users should not have save access to projects.");
    } catch (AccessException ex) {
      // OK
    }
    try {
      projekt.setDescription(who);
      projektDao.update(projekt);
      fail("AccessException expected: " + who + " users should not have update access to projects.");
    } catch (AccessException ex) {
      // OK
    }
  }

}
