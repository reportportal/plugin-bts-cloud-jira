package com.epam.reportportal.extension.jira.command.atlassian;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraClientProvider;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import java.net.URI;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class CloudJiraClientProviderExtended extends CloudJiraClientProvider {
  public CloudJiraClientProviderExtended(BasicTextEncryptor textEncryptor) {
    super(textEncryptor);
  }

  @Override
  public JiraRestClient get(IntegrationParams integrationParams) {
    String providedUsername = CloudJiraProperties.EMAIL.getParam(integrationParams).orElseThrow(
        () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "User email is not specified."
        ));
    String credentials = textEncryptor.decrypt(
        CloudJiraProperties.API_TOKEN.getParam(integrationParams).orElseThrow(
            () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
                "Api token is not specified."
            )));
    String url = CloudJiraProperties.URL.getParam(integrationParams).orElseThrow(
        () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Url to the Cloud Jira is not specified."
        ));
    return createWithBasicHttpAuthentication(URI.create(url), providedUsername, credentials);
  }

  public JiraRestClient create(URI serverUri, AuthenticationHandler authenticationHandler) {
    DisposableHttpClient httpClient =
        (new AsynchronousHttpClientFactory()).createClient(serverUri, authenticationHandler);
    return new AsynchronousJiraRestClientExtended(serverUri, httpClient);
  }

  public JiraRestClient createWithBasicHttpAuthentication(URI serverUri, String username,
      String password) {
    return this.create(
        serverUri,
        (AuthenticationHandler) (new BasicHttpAuthenticationHandler(username, password))
    );
  }
}
