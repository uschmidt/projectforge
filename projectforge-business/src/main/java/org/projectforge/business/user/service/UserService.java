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

package org.projectforge.business.user.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.projectforge.business.configuration.ConfigurationService;
import org.projectforge.business.login.Login;
import org.projectforge.business.login.LoginHandler;
import org.projectforge.business.login.PasswordCheckResult;
import org.projectforge.business.password.PasswordQualityService;
import org.projectforge.business.user.*;
import org.projectforge.common.StringHelper;
import org.projectforge.framework.access.AccessChecker;
import org.projectforge.framework.configuration.SecurityConfig;
import org.projectforge.framework.i18n.I18nKeyAndParams;
import org.projectforge.framework.persistence.api.ModificationStatus;
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext;
import org.projectforge.framework.persistence.user.entities.PFUserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserService.class);

  private static final String MESSAGE_KEY_OLD_PASSWORD_WRONG = "user.changePassword.error.oldPasswordWrong";

  private static final String MESSAGE_KEY_LOGIN_PASSWORD_WRONG = "user.changeWlanPassword.error.loginPasswordWrong";
  private final UsersComparator usersComparator = new UsersComparator();
  private UserGroupCache userGroupCache;
  private ConfigurationService configurationService;
  private UserDao userDao;
  private UserPasswordDao userPasswordDao;
  private AccessChecker accessChecker;
  private UserAuthenticationsService userAuthenticationsService;
  private PasswordQualityService passwordQualityService;

  /**
   * Needed by Wicket for proxying.
   */
  public UserService() {
  }

  @Autowired
  public UserService(AccessChecker accessChecker,
                     ConfigurationService configurationService,
                     PasswordQualityService passwordQualityService,
                     UserDao userDao,
                     UserPasswordDao userPasswordDao,
                     UserGroupCache userGroupCache,
                     UserAuthenticationsService userAuthenticationsService) {
    this.accessChecker = accessChecker;
    this.configurationService = configurationService;
    this.passwordQualityService = passwordQualityService;
    this.userDao = userDao;
    this.userPasswordDao = userPasswordDao;
    this.userGroupCache = userGroupCache;
    this.userAuthenticationsService = userAuthenticationsService;
  }

  /**
   * @param userIds
   * @return
   */
  public List<String> getUserNames(final String userIds) {
    if (StringUtils.isEmpty(userIds)) {
      return null;
    }
    final int[] ids = StringHelper.splitToInts(userIds, ",", false);
    final List<String> list = new ArrayList<>();
    for (final int id : ids) {
      final PFUserDO user = userGroupCache.getUser(id);
      if (user != null) {
        list.add(user.getFullname());
      } else {
        log.warn("User with id '" + id + "' not found in UserGroupCache. userIds string was: " + userIds);
      }
    }
    return list;
  }

  public String getUserMails(final Collection<PFUserDO> users) {
    if (CollectionUtils.isEmpty(users)) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (PFUserDO user : users) {
      String mail = user.getEmail();
      if (StringUtils.isNotBlank(mail)) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(mail);
      }
    }
    return sb.toString();
  }

  /**
   * @return Sorted list of not deleted and not deactivated users.
   */
  public Collection<PFUserDO> getSortedUsers() {
    TreeSet<PFUserDO> sortedUsers = new TreeSet<>(usersComparator);
    final Collection<PFUserDO> allusers = userGroupCache.getAllUsers();
    final PFUserDO loggedInUser = ThreadLocalUserContext.getUser();
    for (final PFUserDO user : allusers) {
      if (!user.isDeleted() && !user.getDeactivated()
          && userDao.hasUserSelectAccess(loggedInUser, user, false)) {
        sortedUsers.add(user);
      }
    }
    return sortedUsers;
  }

  /**
   * @param userIds
   * @return
   */
  public Collection<PFUserDO> getSortedUsers(final String userIds) {
    if (StringUtils.isEmpty(userIds)) {
      return null;
    }
    TreeSet<PFUserDO> sortedUsers = new TreeSet<>(usersComparator);
    final int[] ids = StringHelper.splitToInts(userIds, ",", false);
    for (final int id : ids) {
      final PFUserDO user = userGroupCache.getUser(id);
      if (user != null) {
        sortedUsers.add(user);
      } else {
        log.warn("Group with id '" + id + "' not found in UserGroupCache. groupIds string was: " + userIds);
      }
    }
    return sortedUsers;
  }

  public String getUserIds(final Collection<PFUserDO> users) {
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    for (final PFUserDO user : users) {
      if (user.getId() != null) {
        first = StringHelper.append(buf, first, String.valueOf(user.getId()), ",");
      }
    }
    return buf.toString();
  }

  public List<PFUserDO> getAllUsers() {
    try {
      return userDao.internalLoadAll();
    } catch (final Exception ex) {
      log.error(
          "******* Exception while getting users from data-base (OK only in case of migration from older versions): "
              + ex.getMessage(),
          ex);
      return new ArrayList<>();
    }
  }

  public List<PFUserDO> getAllActiveUsers() {
    return getAllUsers().stream().filter(u -> !u.getDeactivated() && !u.isDeleted()).collect(Collectors.toList());
  }

  /**
   * Checks the given password by comparing it with the stored user password. For backward compatibility the password is
   * encrypted with and without pepper (if configured). The salt string of the given user is used.
   *
   * @param user
   * @param clearTextPassword as clear text.
   * @return true if the password matches the user's password.
   */
  public PasswordCheckResult checkPassword(final PFUserDO user, final char[] clearTextPassword) {
    return userPasswordDao.checkPassword(user, clearTextPassword);
  }

  /**
   * @param userId
   * @return The user from UserGroupCache.
   */
  public PFUserDO getUser(Integer userId) {
    return userGroupCache.getUser(userId);
  }

  /**
   * Encrypts the password with a new generated salt string and the pepper string if configured any.
   *
   * @param user              The user to user.
   * @param clearTextPassword as clear text.x
   * @see UserPasswordDao#encryptAndSavePassword(int, char[])
   */
  public void encryptAndSavePassword(final PFUserDO user, final char[] clearTextPassword) {
    encryptAndSavePassword(user, clearTextPassword, true);
  }

  /**
   * Encrypts the password with a new generated salt string and the pepper string if configured any.
   *
   * @param user              The user to user.
   * @param clearTextPassword as clear text.x
   */
  public void encryptAndSavePassword(final PFUserDO user, final char[] clearTextPassword, final boolean checkAccess) {
    userPasswordDao.encryptAndSavePassword(user.getId(), clearTextPassword, checkAccess);
  }

  /**
   * Changes the user's password. Checks the password quality and the correct authentication for the old password
   * before. Also the stay-logged-in-key will be renewed, so any existing stay-logged-in cookie will be invalid.
   *
   * @param userId
   * @param oldPassword Will be cleared at the end of this method due to security reasons.
   * @param newPassword Will be cleared at the end of this method due to security reasons.
   * @return Error message key if any check failed or null, if successfully changed.
   */
  public List<I18nKeyAndParams> changePassword(final Integer userId, final char[] oldPassword, final char[] newPassword) {
    try {
      Validate.notNull(userId);
      Validate.isTrue(oldPassword.length > 0);
      Validate.isTrue(Objects.equals(userId, ThreadLocalUserContext.getUserId()), "User is only allowed to change his own password-");
      final PFUserDO user = userDao.internalGetById(userId);
      final PFUserDO userCheck = getUser(user.getUsername(), oldPassword, false);
      if (userCheck == null) {
        return Collections.singletonList(new I18nKeyAndParams(MESSAGE_KEY_OLD_PASSWORD_WRONG));
      }
      return doPasswordChange(user, oldPassword, newPassword);
    } finally {
      LoginHandler.clearPassword(newPassword);
      LoginHandler.clearPassword(oldPassword);
    }
  }

  /**
   * Changes the user's password. Checks the password quality.
   * Also the stay-logged-in-key will be renewed, so any existing stay-logged-in cookie will be invalid.
   *
   * @param userId
   * @param newPassword Will be cleared at the end of this method due to security reasons.
   * @return Error message key if any check failed or null, if successfully changed.
   */
  public List<I18nKeyAndParams> changePasswordByAdmin(final Integer userId, final char[] newPassword) {
    try {
      Validate.notNull(userId);
      Validate.isTrue(!Objects.equals(userId, ThreadLocalUserContext.getUserId()), "Admin user is not allowed to change his own password without entering his login password-");
      accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
      final PFUserDO user = userDao.internalGetById(userId);
      return doPasswordChange(user, null, newPassword);
    } finally {
      LoginHandler.clearPassword(newPassword);
    }
  }

  private List<I18nKeyAndParams> doPasswordChange(final PFUserDO user, final char[] oldPassword, final char[] newPassword) {
    Validate.notNull(user);
    Validate.isTrue(newPassword.length > 0);
    final List<I18nKeyAndParams> errorMsgKeys = passwordQualityService.checkPasswordQuality(oldPassword, newPassword);
    if (!errorMsgKeys.isEmpty()) {
      return errorMsgKeys;
    }
    encryptAndSavePassword(user, newPassword);
    onPasswordChange(user, true);
    userDao.internalUpdate(user);
    Login.getInstance().passwordChanged(user, newPassword);
    log.info("Password changed for user: " + user.getId() + " - " + user.getUsername());
    return Collections.emptyList();
  }

  /**
   * Should be called only by password reset (authentication via 2FA.
   * Changes the user's password. Checks the password quality before.
   * Also the stay-logged-in-key will be renewed, so any existing stay-logged-in cookie will be invalid.
   *
   * @param userId
   * @param newPassword Will be cleared at the end of this method due to security reasons.
   * @return Error message key if any check failed or null, if successfully changed.
   */
  public List<I18nKeyAndParams> internalChangePasswordAfter2FA(final Integer userId, final char[] newPassword) {
    try {
      Validate.notNull(userId);
      Validate.isTrue(newPassword.length > 0);
      Validate.isTrue(ThreadLocalUserContext.getUser() == null, "ThreadLocalUser mustn't be given on password reset.");

      final List<I18nKeyAndParams> errorMsgKeys = passwordQualityService.checkPasswordQuality(newPassword);
      if (!errorMsgKeys.isEmpty()) {
        return errorMsgKeys;
      }
      final PFUserDO user = userDao.internalGetById(userId);
      ThreadLocalUserContext.setUser(user);
      encryptAndSavePassword(user, newPassword);
      onPasswordChange(user, true);
      userDao.internalUpdate(user);
      Login.getInstance().passwordChanged(user, newPassword);
      log.info("Password changed for user: " + user.getId() + " - " + user.getUsername());
      return Collections.emptyList();
    } finally {
      ThreadLocalUserContext.setUserContext(null);
      LoginHandler.clearPassword(newPassword);
    }
  }

  /**
   * Changes the user's WLAN password. Checks the password quality and the correct authentication for the login password before.
   *
   * @param user
   * @param loginPassword   Will be cleared at the end of this method due to security reasons
   * @param newWlanPassword Will be cleared at the end of this method due to security reasons
   * @return Error message key if any check failed or null, if successfully changed.
   */
  public List<I18nKeyAndParams> changeWlanPassword(PFUserDO user, final char[] loginPassword, final char[] newWlanPassword) {
    try {
      Validate.notNull(user);
      Validate.isTrue(loginPassword.length > 0);
      Validate.isTrue(Objects.equals(user.getId(), ThreadLocalUserContext.getUserId()), "User is only allowed to change his own Wlan/Samba password-");
      user = getUser(user.getUsername(), loginPassword, false); // get user from DB to persist the change of the wlan password time
      if (user == null) {
        return Collections.singletonList(new I18nKeyAndParams(MESSAGE_KEY_LOGIN_PASSWORD_WRONG));
      }
      return doWlanPasswordChange(user, newWlanPassword);
    } finally {
      LoginHandler.clearPassword(loginPassword);
      LoginHandler.clearPassword(newWlanPassword);
    }
  }

  /**
   * Changes the user's password. Checks the password quality.
   * Also the stay-logged-in-key will be renewed, so any existing stay-logged-in cookie will be invalid.
   *
   * @param userId
   * @param newPassword Will be cleared at the end of this method due to security reasons.
   * @return Error message key if any check failed or null, if successfully changed.
   */
  public List<I18nKeyAndParams> changeWlanPasswordByAdmin(final PFUserDO user, final char[] newWlanPassword) {
    try {
      Validate.notNull(user);
      Validate.isTrue(!Objects.equals(user.getId(), ThreadLocalUserContext.getUserId()), "Admin user is not allowed to change his own password without entering his login password-");
      accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
      return doWlanPasswordChange(user, newWlanPassword);
    } finally {
      LoginHandler.clearPassword(newWlanPassword);
    }
  }

  private List<I18nKeyAndParams> doWlanPasswordChange(final PFUserDO user, final char[] newWlanPassword) {
    onWlanPasswordChange(user, true); // set last change time and creaty history entry
    Login.getInstance().wlanPasswordChanged(user, newWlanPassword); // change the wlan password
    log.info("WLAN Password changed for user: " + user.getId() + " - " + user.getUsername());
    return Collections.emptyList();
  }



  public void onPasswordChange(final PFUserDO user, final boolean createHistoryEntry) {
    userPasswordDao.onPasswordChange(user, createHistoryEntry);
  }

  public void onWlanPasswordChange(final PFUserDO user, final boolean createHistoryEntry) {
    userPasswordDao.onWlanPasswordChange(user, createHistoryEntry);
  }

  /**
   * @param username
   * @param password
   * @param updateSaltAndPepperIfNeeded
   * @return
   */
  @SuppressWarnings("unchecked")
  protected PFUserDO getUser(final String username, final char[] password, final boolean updateSaltAndPepperIfNeeded) {
    final List<PFUserDO> list = userDao.findByUsername(username);
    if (list == null || list.isEmpty() || list.get(0) == null) {
      return null;
    }
    final PFUserDO user = list.get(0);
    final PasswordCheckResult passwordCheckResult = userPasswordDao.checkPassword(user, password);
    if (!passwordCheckResult.isOK()) {
      return null;
    }
    if (updateSaltAndPepperIfNeeded && passwordCheckResult.isPasswordUpdateNeeded()) {
      log.info("Giving salt and/or pepper to the password of the user " + user.getId() + ".");
      encryptAndSavePassword(user, password, false);
      userDao.internalUpdate(user);
    }
    return user;
  }

  /**
   * Ohne Zugangsbegrenzung. Wird bei Anmeldung benötigt.
   *
   * @param username
   * @param password
   */
  public PFUserDO authenticateUser(final String username, final char[] password) {
    Validate.notNull(username);
    Validate.isTrue(password.length > 0);

    PFUserDO user = getUser(username, password, true);
    if (user != null) {

      final int loginFailures = user.getLoginFailures();
      final Date lastLogin = user.getLastLogin();
      userDao.updateUserAfterLoginSuccess(user);
      if (!user.hasSystemAccess()) {
        log.warn("Deleted/deactivated user tried to login: " + user);
        return null;
      }
      user.setLoginFailures(loginFailures); // Restore loginFailures for current user session.
      user.setLastLogin(lastLogin); // Restore lastLogin for current user session.
      return user;
    }
    userDao.updateIncrementLoginFailure(username);
    return null;
  }

  private String getPepperString() {
    final SecurityConfig securityConfig = configurationService.getSecurityConfig();
    if (securityConfig != null) {
      return securityConfig.getPasswordPepper();
    }
    return "";
  }

  public PFUserDO getInternalByUsername(String username) {
    return userDao.getInternalByName(username);
  }

  /**
   * @param id
   * @return the user from db (UserDao).
   */
  public PFUserDO getById(Serializable id) {
    return userDao.getById(id);
  }

  public PFUserDO internalGetById(Serializable id) {
    return userDao.internalGetById(id);
  }

  public Integer save(PFUserDO user) {
    return userDao.internalSave(user);
  }

  public void markAsDeleted(PFUserDO user) {
    userDao.internalMarkAsDeleted(user);
  }

  public boolean doesUsernameAlreadyExist(PFUserDO user) {
    return userDao.doesUsernameAlreadyExist(user);
  }

  public ModificationStatus update(PFUserDO user) {
    return userDao.update(user);
  }

  /**
   * Without access checking!!! Secret fields are cleared.
   *
   * @see UserDao#internalLoadAll()
   */
  public List<PFUserDO> internalLoadAll() {
    return userDao.internalLoadAll();
  }

  public String getNormalizedPersonalPhoneIdentifiers(final PFUserDO user) {
    return getNormalizedPersonalPhoneIdentifiers(user.getPersonalPhoneIdentifiers());
  }

  public String getNormalizedPersonalPhoneIdentifiers(final String personalPhoneIdentifiers) {
    if (StringUtils.isNotBlank(personalPhoneIdentifiers)) {
      final String[] ids = getPersonalPhoneIdentifiers(personalPhoneIdentifiers);
      if (ids != null) {
        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (final String id : ids) {
          if (first) {
            first = false;
          } else {
            buf.append(",");
          }
          buf.append(id);
        }
        return buf.toString();
      }
    }
    return null;
  }

  public String[] getPersonalPhoneIdentifiers(final PFUserDO user) {
    return getPersonalPhoneIdentifiers(user.getPersonalPhoneIdentifiers());
  }

  public String[] getPersonalPhoneIdentifiers(final String personalPhoneIdentifiers) {
    final String[] tokens = StringUtils.split(personalPhoneIdentifiers, ", ;|");
    if (tokens == null) {
      return null;
    }
    int n = 0;
    for (final String token : tokens) {
      if (StringUtils.isNotBlank(token)) {
        n++;
      }
    }
    if (n == 0) {
      return null;
    }
    final String[] result = new String[n];
    n = 0;
    for (final String token : tokens) {
      if (StringUtils.isNotBlank(token)) {
        result[n] = token.trim();
        n++;
      }
    }
    return result;
  }

  public UserDao getUserDao() {
    return userDao;
  }

  public void updateMyAccount(PFUserDO data) {
    userDao.updateMyAccount(data);
  }

  public void undelete(PFUserDO dbUser) {
    userDao.internalUndelete(dbUser);
  }

  public List<PFUserDO> findUserByMail(String email) {
    List<PFUserDO> userList = new ArrayList<>();
    for (PFUserDO user : userGroupCache.getAllUsers()) {
      if (user.getEmail() != null && user.getEmail().toLowerCase().equals(email.toLowerCase())) {
        userList.add(user);
      }
    }
    return userList;
  }

  /**
   * Encrypts the given data with the user's password hash. If the user changes his password, decryption isn't possible
   * anymore.
   *
   * @param data The data to encrypt.
   * @return The encrypted data.
   * @see UserDao#encrypt(String)
   */
  public String encrypt(String data) {
    return userDao.encrypt(data);
  }

  /**
   * Decrypts the given data with the user's password hash. If the user changes his password, decryption isn't possible
   * anymore.
   *
   * @param encrypted The data to encrypt.
   * @return The decrypted data.
   * @see UserDao#decrypt(String)
   */
  public String decrypt(String encrypted) {
    return userDao.decrypt(encrypted);
  }

  /**
   * Decrypts the given data with the user's password hash. If the user changes his password, decryption isn't possible
   * anymore.
   *
   * @param encrypted The data to encrypt.
   * @param userId    Use the password of the given user (used by CookieService, because user isn't yet logged-in).
   * @return The decrypted data.
   * @see UserDao#decrypt(String)
   */
  public String decrypt(String encrypted, Integer userId) {
    return userDao.decrypt(encrypted, userId);
  }
}
