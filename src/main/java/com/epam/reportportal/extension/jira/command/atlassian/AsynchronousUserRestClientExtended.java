/*
 * Copyright 2024 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.reportportal.extension.jira.command.atlassian;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.jira.rest.client.internal.json.UsersJsonParser;
import io.atlassian.util.concurrent.Promise;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class AsynchronousUserRestClientExtended extends AbstractAsynchronousRestClient {

  private static final String USER_URI_PREFIX = "user";
  private static final String SEARCH_URI_PREFIX = "search";

  private static final String QUERY_ATTRIBUTE = "query";
  private static final String START_AT_ATTRIBUTE = "startAt";
  private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
  private static final String INCLUDE_ACTIVE_ATTRIBUTE = "includeActive";
  private static final String INCLUDE_INACTIVE_ATTRIBUTE = "includeInactive";

  private final UsersJsonParser usersJsonParser = new UsersJsonParser();

  private final URI baseUri;

  public AsynchronousUserRestClientExtended(URI serverUri,
      HttpClient client) {
    super(client);
    this.baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
  }

  public Promise<Iterable<User>> findUsers(String username) {
    return findUsers(username, null, null, null, null);
  }

  public Promise<Iterable<User>> findUsers(String username, @Nullable Integer startAt,
      @Nullable Integer maxResults, @Nullable Boolean includeActive,
      @Nullable Boolean includeInactive) {
    UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX).path(SEARCH_URI_PREFIX)
        .queryParam(QUERY_ATTRIBUTE, username);

    addOptionalQueryParam(uriBuilder, START_AT_ATTRIBUTE, startAt);
    addOptionalQueryParam(uriBuilder, MAX_RESULTS_ATTRIBUTE, maxResults);
    addOptionalQueryParam(uriBuilder, INCLUDE_ACTIVE_ATTRIBUTE, includeActive);
    addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);
    addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);
    addOptionalQueryParam(uriBuilder, INCLUDE_INACTIVE_ATTRIBUTE, includeInactive);

    final URI usersUri = uriBuilder.build();
    return getAndParse(usersUri, usersJsonParser);
  }

  private void addOptionalQueryParam(final UriBuilder uriBuilder, final String key, final Object value) {
    if (value != null) {
      uriBuilder.queryParam(key, value);
    }
  }
}
