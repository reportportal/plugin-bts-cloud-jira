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
package com.epam.reportportal.extension.jira.event.plugin;

import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.jira.event.EventHandlerFactory;
import com.epam.reportportal.extension.jira.event.handler.EventHandler;
import com.epam.reportportal.extension.jira.event.handler.plugin.PluginLoadedEventHandler;
import com.epam.reportportal.extension.jira.info.PluginInfoProvider;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginEventHandlerFactory implements EventHandlerFactory<PluginEvent> {

	public static final String LOAD_KEY = "load";

	private final Map<String, EventHandler<PluginEvent>> eventHandlerMapping;

	public PluginEventHandlerFactory(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			PluginInfoProvider pluginInfoProvider) {
		this.eventHandlerMapping = new HashMap<>();
		this.eventHandlerMapping.put(LOAD_KEY,
				new PluginLoadedEventHandler(integrationTypeRepository, integrationRepository, pluginInfoProvider)
		);
	}

	@Override
	public EventHandler<PluginEvent> getEventHandler(String key) {
		return eventHandlerMapping.get(key);
	}
}
