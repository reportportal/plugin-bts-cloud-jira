package com.epam.reportportal.extension.jira.command;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.epam.reportportal.extension.ProjectManagerCommand;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.reportportal.extension.jira.command.utils.CloudJiraClientUtils.getClient;
import static com.epam.reportportal.extension.jira.command.utils.CloudJiraClientUtils.getProject;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetIssueFieldsCommand extends ProjectManagerCommand<List<PostFormField>> {

	private static final String ISSUE_TYPE = "issueType";
	private static final Logger LOGGER = LoggerFactory.getLogger(GetIssueFieldsCommand.class);

	public GetIssueFieldsCommand(ProjectRepository projectRepository) {
		super(projectRepository);
	}

	@Override
	public String getName() {
		return "getIssueFields";
	}

	@Override
	protected List<PostFormField> invokeCommand(Integration integration, Map<String, Object> params) {
		List<PostFormField> result = new ArrayList<>();

		final String issueTypeParam = Optional.ofNullable(params.get(ISSUE_TYPE))
				.map(it -> (String) it)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Issue type is not provided"));

		try (JiraRestClient client = getClient(integration.getParams())) {
			Project jiraProject = getProject(client, integration.getParams());
			Optional<IssueType> issueType = StreamSupport.stream(jiraProject.getIssueTypes().spliterator(), false)
					.filter(input -> issueTypeParam.equalsIgnoreCase(input.getName()))
					.findFirst();

			BusinessRule.expect(issueType, Preconditions.IS_PRESENT)
					.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Issue type '" + issueTypeParam + "' not found");

			GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder().withExpandedIssueTypesFields()
					.withProjectKeys(jiraProject.getKey())
					.build();
			Iterable<CimProject> projects = client.getIssueClient().getCreateIssueMetadata(options).claim();
			CimProject project = projects.iterator().next();
			CimIssueType cimIssueType = EntityHelper.findEntityById(project.getIssueTypes(), issueType.get().getId());
			for (String key : cimIssueType.getFields().keySet()) {
				List<String> defValue = null;
				CimFieldInfo issueField = cimIssueType.getFields().get(key);
				// Field ID for next JIRA POST ticket requests
				String fieldID = issueField.getId();
				String fieldType = issueField.getSchema().getType();
				List<AllowedValue> allowed = new ArrayList<>();

				// Provide values for custom fields with predefined options
				if (issueField.getAllowedValues() != null) {
					for (Object o : issueField.getAllowedValues()) {
						if (o instanceof CustomFieldOption) {
							CustomFieldOption customField = (CustomFieldOption) o;
							allowed.add(new AllowedValue(String.valueOf(customField.getId()), (customField).getValue()));
						}
					}
				}

				// Field NAME for user friendly UI output (for UI only)
				String fieldName = issueField.getName();

				if (fieldID.equalsIgnoreCase(IssueFieldId.COMPONENTS_FIELD.id)) {
					for (BasicComponent component : jiraProject.getComponents()) {
						allowed.add(new AllowedValue(String.valueOf(component.getId()), component.getName()));
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.FIX_VERSIONS_FIELD.id)) {
					for (Version version : jiraProject.getVersions()) {
						allowed.add(new AllowedValue(String.valueOf(version.getId()), version.getName()));
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.AFFECTS_VERSIONS_FIELD.id)) {
					for (Version version : jiraProject.getVersions()) {
						allowed.add(new AllowedValue(String.valueOf(version.getId()), version.getName()));
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.PRIORITY_FIELD.id)) {
					if (null != cimIssueType.getField(IssueFieldId.PRIORITY_FIELD)) {
						Iterable<Object> allowedValuesForPriority = cimIssueType.getField(IssueFieldId.PRIORITY_FIELD).getAllowedValues();
						for (Object singlePriority : allowedValuesForPriority) {
							BasicPriority priority = (BasicPriority) singlePriority;
							allowed.add(new AllowedValue(String.valueOf(priority.getId()), priority.getName()));
						}
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.ISSUE_TYPE_FIELD.id)) {
					defValue = Collections.singletonList(issueTypeParam);
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.ASSIGNEE_FIELD.id)) {
					allowed = getJiraProjectAssignee(jiraProject);
				}

				//@formatter:off
				// Skip project field as external from list
				// Skip attachment cause we are not providing this functionality now
				// Skip timetracking field cause complexity. There are two fields with Original Estimation and Remaining Estimation.
				// Skip Story Link as greenhopper plugin field.
				// Skip Sprint field as complex one.
				//@formatter:on
				if ("project".equalsIgnoreCase(fieldID) || "attachment".equalsIgnoreCase(fieldID)
						|| "timetracking".equalsIgnoreCase(fieldID) || "Epic Link".equalsIgnoreCase(fieldName) || "Sprint".equalsIgnoreCase(
						fieldName)) {
					continue;
				}

				result.add(new PostFormField(fieldID, fieldName, fieldType, issueField.isRequired(), defValue, allowed));
			}
			return result;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	/**
	 * Get list of project users available for assignee field
	 *
	 * @param jiraProject Project from JIRA
	 * @return List of allowed values
	 */
	private List<AllowedValue> getJiraProjectAssignee(Project jiraProject) {
		Iterable<BasicProjectRole> jiraProjectRoles = jiraProject.getProjectRoles();
		try {
			return StreamSupport.stream(jiraProjectRoles.spliterator(), false)
					.filter(role -> role instanceof ProjectRole)
					.map(role -> (ProjectRole) role)
					.flatMap(role -> StreamSupport.stream(role.getActors().spliterator(), false))
					.distinct()
					.map(actor -> new AllowedValue(String.valueOf(actor.getId()), actor.getDisplayName()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ReportPortalException("There is a problem while getting issue types", e);
		}

	}
}
