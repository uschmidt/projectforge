/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.model.rest;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class RestPaths
{
  public static final String REST = "/rest";

  public static final String PUBLIC_REST = "/publicRest";

  public static final String REST_WEB_APP = "rs";

  public static final String REST_WEB_APP_PUBLIC = "rsPublic";

  public static final String AUTHENTICATE = REST + "/authenticate";

  public static final String AUTHENTICATE_GET_TOKEN_METHOD = "getToken";

  public static final String AUTHENTICATE_GET_TOKEN = AUTHENTICATE + "/" + AUTHENTICATE_GET_TOKEN_METHOD;

  public static final String AUTHENTICATE_INITIAL_CONTACT_METHOD = "initialContact";

  public static final String AUTHENTICATE_INITIAL_CONTACT = AUTHENTICATE + "/" + AUTHENTICATE_INITIAL_CONTACT_METHOD;

  public static final String LIST = "list";

  public static final String CANCEL = "cancel";

  public static final String EDIT = "edit";

  public static final String SAVE = "save";

  public static final String UPDATE = "update";

  public static final String SAVE_OR_UDATE = SAVE + "or" + UPDATE;

  public static final String DELETE = "delete";

  public static final String MARK_AS_DELETED = "markAsDeleted";

  public static final String UNDELETE = "undelete";

  public static final String CLONE = "clone";

  public static final String VERSION_CHECK = PUBLIC_REST + "/versionCheck";

  public static final String FILTER_RESET = "filterReset";
}
