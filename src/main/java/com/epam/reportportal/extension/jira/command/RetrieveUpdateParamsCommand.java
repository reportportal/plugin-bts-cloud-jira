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

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.jira.command.utils.CloudJiraProperties;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class RetrieveUpdateParamsCommand implements CommonPluginCommand<Map<String, Object>> {

	private final BasicTextEncryptor textEncryptor;

	public RetrieveUpdateParamsCommand(BasicTextEncryptor textEncryptor) {
		this.textEncryptor = textEncryptor;
	}

	@Override
	public String getName() {
		return "retrieveUpdated";
	}

	@Override
	//@param integration is always null because it can be not saved yet
	public Map<String, Object> executeCommand(Map<String, Object> integrationParams) {
		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(integrationParams.size());
		CloudJiraProperties.URL.getParam(integrationParams).ifPresent(url -> resultParams.put(CloudJiraProperties.URL.getName(), url));
		CloudJiraProperties.PROJECT.getParam(integrationParams)
				.ifPresent(url -> resultParams.put(CloudJiraProperties.PROJECT.getName(), url));
		CloudJiraProperties.EMAIL.getParam(integrationParams).ifPresent(url -> resultParams.put(CloudJiraProperties.EMAIL.getName(), url));
		CloudJiraProperties.API_TOKEN.getParam(integrationParams)
				.ifPresent(token -> resultParams.put(CloudJiraProperties.API_TOKEN.getName(), textEncryptor.encrypt(token)));
		Optional.ofNullable(integrationParams.get("defectFormFields"))
				.ifPresent(defectFormFields -> resultParams.put("defectFormFields", defectFormFields));
		return resultParams;
	}
}
