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

package org.projectforge.rest;

import org.projectforge.ProjectForgeVersion;
import org.projectforge.model.rest.RestPaths;
import org.projectforge.model.rest.ServerInfo;
import org.projectforge.model.rest.UserObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class RestClientMain {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestClientMain.class);

  private static String url, username, password;

  public static void main(final String[] args) throws IOException {
    final Client client = ClientBuilder.newClient();
    final UserObject user = authenticate(client);
    initialContact(client, user);
  }

  public static UserObject authenticate(final Client client) throws IOException {
    initialize();
    return authenticate(client, username, password);
  }

  /**
   * @return authentication token for further rest calls.
   */
  public static UserObject authenticate(final Client client, final String username, final String password) throws IOException {
    initialize();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON);

    // http://localhost:8080/ProjectForge/rest/authenticate/getToken // username / password
    final String url = getUrl() + RestPaths.AUTHENTICATE_GET_TOKEN;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam(Authentication.AUTHENTICATION_USERNAME, username)
            .queryParam(Authentication.AUTHENTICATION_PASSWORD, password);
    HttpEntity<?> entity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();
    UserObject user = restTemplate.getForObject(builder.toUriString(), UserObject.class);

    if (user == null) {
      throw new RuntimeException("Can't  get user.");
    }
    final Integer userId = user.getId();
    final String authenticationToken = user.getAuthenticationToken();
    log.info("userId = " + userId + ", authenticationToken=" + authenticationToken);
    return user;
  }

  public static WebTarget setConnectionSettings(final WebTarget webResource, final ConnectionSettings settings) {
    if (settings == null) {
      return webResource;
    }
    WebTarget res = webResource;
    if (settings.isDefaultDateTimeFormat() == false) {
      res = webResource.queryParam(ConnectionSettings.DATE_TIME_FORMAT, settings.getDateTimeFormat().toString());
    }
    return res;
  }

  public static void initialContact(final Client client, final UserObject user) throws IOException {
    initialize();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON);
    headers.set(Authentication.AUTHENTICATION_USER_ID, user.getId().toString());
    headers.set(Authentication.AUTHENTICATION_TOKEN, user.getAuthenticationToken());

    // http://localhost:8080/ProjectForge/rest/authenticate/initialContact?clientVersion=5.0 // userId / token
    final String url = getUrl() + RestPaths.AUTHENTICATE_GET_TOKEN;

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("clientVersion", ProjectForgeVersion.VERSION_STRING);
    HttpEntity<?> entity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();
    ServerInfo serverInfo = restTemplate.exchange(
            builder.toUriString(),
            HttpMethod.GET,
            entity,
            ServerInfo.class)
            .getBody();

    if (serverInfo == null) {
      throw new RuntimeException("Can't get serverInfo.");
    }
    log.info("serverInfo=" + serverInfo);
  }

  public static String getUrl() {
    return url;
  }

  private static void initialize() {
    if (username != null) {
      // Already initialized.
      return;
    }
    final String filename = System.getProperty("user.home") + "/ProjectForge/restauthentification.properties";
    Properties prop = null;
    FileReader reader = null;
    {
      try {
        reader = new FileReader(filename);
        prop = new Properties();
        prop.load(reader);
      } catch (final FileNotFoundException ex) {
        prop = null;
      } catch (final IOException ex) {
        prop = null;
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (final IOException ex) {
            prop = null;
          }
        }
      }
    }
    if (prop != null) {
      username = prop.getProperty("user");
      if (username == null) {
        log.warn("Property 'user' not found in '" + filename + "'. Assuming 'demo'.");
      }
      password = prop.getProperty("password");
      if (password == null) {
        log.warn("Property 'password' not found in '" + filename + "'. Assuming 'demo123'.");
      }
      url = prop.getProperty("url");
      if (url == null) {
        log.warn("Property 'url' not found in '" + filename + "'. Assuming 'http://localhost:8080/ProjectForge'.");
      }
    }
    if (username == null) {
      username = "demo";
    }
    if (password == null) {
      password = "demo123";
    }
    if (url == null) {
      url = "http://localhost:8080";
    }
    if (prop == null) {
      log.info("For customized url and username/password please create file '"
              + filename
              + "' with following content:\n# For rest test calls\nurl="
              + url
              + "\nuser="
              + username
              + "\npassword="
              + password
              + "\n");
    }
    log.info("Testing with user '" + username + "': " + url);
  }
}
