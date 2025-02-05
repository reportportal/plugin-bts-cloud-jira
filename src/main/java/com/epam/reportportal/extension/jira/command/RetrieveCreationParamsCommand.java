/*
 * Copyright 2021 EPAM Systems
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
package com.epam.reportportal.extension.jira.command;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;


/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class RetrieveCreationParamsCommand implements CommonPluginCommand<Map<String, Object>> {

	private final BasicTextEncryptor textEncryptor;

	public RetrieveCreationParamsCommand(BasicTextEncryptor textEncryptor) {
		this.textEncryptor = textEncryptor;
	}

	@Override
	public String getName() {
		return "retrieveCreate";
	}

	@Override
	//@param integration is always null because it can be not saved yet
	public Map<String, Object> executeCommand(Map<String, Object> integrationParams) {

		expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(CloudJiraProperties.values().length);

		resultParams.put(CloudJiraProperties.PROJECT.getName(),
				CloudJiraProperties.PROJECT.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "BTS project is not specified."))
		);
		resultParams.put(CloudJiraProperties.URL.getName(),
				CloudJiraProperties.URL.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "BTS url is not specified."))
		);


		resultParams.put(CloudJiraProperties.EMAIL.getName(),
				CloudJiraProperties.EMAIL.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Email value is not specified."))
		);

		resultParams.put(CloudJiraProperties.API_TOKEN.getName(),
				textEncryptor.encrypt(CloudJiraProperties.API_TOKEN.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "API token value is not specified.")))
		);

		return resultParams;
	}
}
