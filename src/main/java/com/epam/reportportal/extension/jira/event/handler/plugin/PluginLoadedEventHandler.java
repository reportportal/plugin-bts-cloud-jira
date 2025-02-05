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
package com.epam.reportportal.extension.jira.event.handler.plugin;

import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.jira.event.handler.EventHandler;
import com.epam.reportportal.extension.jira.info.PluginInfoProvider;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginLoadedEventHandler implements EventHandler<PluginEvent> {

	private final IntegrationTypeRepository integrationTypeRepository;
	private final IntegrationRepository integrationRepository;
	private final PluginInfoProvider pluginInfoProvider;

	public PluginLoadedEventHandler(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			PluginInfoProvider pluginInfoProvider) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.integrationRepository = integrationRepository;
		this.pluginInfoProvider = pluginInfoProvider;
	}

	@Override
	public void handle(PluginEvent event) {
		integrationTypeRepository.findByName(event.getPluginId()).ifPresent(integrationType -> {
			createIntegration(event.getPluginId(), integrationType);
			integrationTypeRepository.save(pluginInfoProvider.provide(integrationType));
		});
	}

	private void createIntegration(String name, IntegrationType integrationType) {
		List<Integration> integrations = integrationRepository.findAllGlobalByType(integrationType);
		if (integrations.isEmpty()) {
			Integration integration = new Integration();
			integration.setName(name);
			integration.setType(integrationType);
			integration.setCreationDate(Instant.now());
			integration.setEnabled(true);
			integration.setCreator("SYSTEM");
			integration.setParams(new IntegrationParams(new HashMap<>()));
			integrationRepository.save(integration);
		}
	}

}
