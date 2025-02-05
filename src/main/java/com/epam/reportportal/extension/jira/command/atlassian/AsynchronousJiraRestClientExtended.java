package com.epam.reportportal.extension.jira.command.atlassian;

import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.GroupRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousComponentRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousGroupRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousMetadataRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousProjectRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousProjectRolesRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousSearchRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousSessionRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousUserRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousVersionRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class AsynchronousJiraRestClientExtended implements JiraRestClient {

	private final IssueRestClient issueRestClient;
	private final SessionRestClient sessionRestClient;
	private final UserRestClient userRestClient;
	private final GroupRestClient groupRestClient;
	private final ProjectRestClient projectRestClient;
	private final ComponentRestClient componentRestClient;
	private final MetadataRestClient metadataRestClient;
	private final SearchRestClient searchRestClient;
	private final VersionRestClient versionRestClient;
	private final ProjectRolesRestClient projectRolesRestClient;
	private final MyPermissionsRestClient myPermissionsRestClient;
	private final DisposableHttpClient httpClient;
	private final AuditRestClient auditRestClient;

	public AsynchronousJiraRestClientExtended(final URI serverUri, final DisposableHttpClient httpClient) {
		final URI baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();

		this.httpClient = httpClient;
		metadataRestClient = new AsynchronousMetadataRestClient(baseUri, httpClient);
		sessionRestClient = new AsynchronousSessionRestClient(serverUri, httpClient);
		issueRestClient = new AsynchronousIssueRestClientExtended(baseUri, httpClient, sessionRestClient, metadataRestClient);
		userRestClient = new AsynchronousUserRestClient(baseUri, httpClient);
		groupRestClient  = new AsynchronousGroupRestClient(baseUri, httpClient);
		projectRestClient = new AsynchronousProjectRestClient(baseUri, httpClient);
		componentRestClient = new AsynchronousComponentRestClient(baseUri, httpClient);
		searchRestClient = new AsynchronousSearchRestClient(baseUri, httpClient);
		versionRestClient = new AsynchronousVersionRestClient(baseUri, httpClient);
		projectRolesRestClient = new AsynchronousProjectRolesRestClient(serverUri, httpClient);
		myPermissionsRestClient = null;
		auditRestClient = null;
	}

	@Override
	public IssueRestClient getIssueClient() {
		return issueRestClient;
	}

	@Override
	public SessionRestClient getSessionClient() {
		return sessionRestClient;
	}

	@Override
	public UserRestClient getUserClient() {
		return userRestClient;
	}

	@Override
	public GroupRestClient getGroupClient() {
		return groupRestClient;
	}

	@Override
	public ProjectRestClient getProjectClient() {
		return projectRestClient;
	}

	@Override
	public ComponentRestClient getComponentClient() {
		return componentRestClient;
	}

	@Override
	public MetadataRestClient getMetadataClient() {
		return metadataRestClient;
	}

	@Override
	public SearchRestClient getSearchClient() {
		return searchRestClient;
	}

	@Override
	public VersionRestClient getVersionRestClient() {
		return versionRestClient;
	}

	@Override
	public ProjectRolesRestClient getProjectRolesRestClient() {
		return projectRolesRestClient;
	}

	@Override
	public MyPermissionsRestClient getMyPermissionsRestClient() {
		return myPermissionsRestClient;
	}

	@Override
	public AuditRestClient getAuditRestClient() {
		return auditRestClient;
	}

	@Override
	public void close() throws IOException {
		try {
			httpClient.destroy();
		} catch (Exception e) {
			throw (e instanceof IOException) ? ((IOException) e) : new IOException(e);
		}
	}
}
