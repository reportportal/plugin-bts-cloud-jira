package com.epam.reportportal.extension.jira.command.atlassian;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import com.google.common.base.Joiner;
import io.atlassian.util.concurrent.Promise;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class AsynchronousIssueRestClientExtended extends AsynchronousIssueRestClient {

	private final URI baseUri;

	public AsynchronousIssueRestClientExtended(URI baseUri, HttpClient client, SessionRestClient sessionRestClient, MetadataRestClient metadataRestClient) {
		super(baseUri, client, sessionRestClient, metadataRestClient);
		this.baseUri = baseUri;
	}

	@Override
	public Promise<Iterable<CimProject>> getCreateIssueMetadata(@Nullable GetCreateIssueMetadataOptions options) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(UriBuilder.fromUri(baseUri).build()).path("issue/createmeta");

		if (options != null) {
			if (options.projectIds != null) {
				uriBuilder.queryParam("projectIds", Joiner.on(",").join(options.projectIds));
			}

			if (options.projectKeys != null) {
				uriBuilder.queryParam("projectKeys", Joiner.on(",").join(options.projectKeys));
			}

			if (options.issueTypeIds != null) {
				uriBuilder.queryParam("issuetypeIds", Joiner.on(",").join(options.issueTypeIds));
			}

			final Iterable<String> issueTypeNames = options.issueTypeNames;
			if (issueTypeNames != null) {
				for (final String name : issueTypeNames) {
					uriBuilder.queryParam("issuetypeNames", name);
				}
			}

			final Iterable<String> expandos = options.expandos;
			if (expandos != null && expandos.iterator().hasNext()) {
				uriBuilder.queryParam("expand", Joiner.on(",").join(expandos));
			}
		}

		return getAndParse(uriBuilder.build(), new CreateIssueMetadataJsonParserExtended());
	}
}
