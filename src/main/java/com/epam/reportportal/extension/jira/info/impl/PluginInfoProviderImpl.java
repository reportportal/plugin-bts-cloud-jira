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

package com.epam.reportportal.extension.jira.info.impl;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.jira.info.PluginInfoProvider;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginInfoProviderImpl implements PluginInfoProvider {

  private static final String BINARY_DATA_KEY = "binaryData";
  private static final String DESCRIPTION_KEY = "description";
  private static final String METADATA_KEY = "metadata";

  private static final String PLUGIN_DESCRIPTION =
      "The integration provides an exchange of information between ReportPortal and the JIRA Cloud, such as posting issues and linking issues, getting updates on their statuses.";

  public static final Map<String, Object> PLUGIN_METADATA = new HashMap<>();

  static {
    PLUGIN_METADATA.put("embedded", true);
    PLUGIN_METADATA.put("multiple", true);
  }

  private final String resourcesDir;
  private final String propertyFile;

  public PluginInfoProviderImpl(String resourcesDir, String propertyFile) {
    this.resourcesDir = resourcesDir;
    this.propertyFile = propertyFile;
  }

  @Override
  public IntegrationType provide(IntegrationType integrationType) {
    loadBinaryDataInfo(integrationType);
    updateDescription(integrationType);
    updateMetadata(integrationType);
    return integrationType;
  }

  private void loadBinaryDataInfo(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    if (ofNullable(details.get(BINARY_DATA_KEY)).isEmpty()) {
      try (InputStream propertiesStream = Files.newInputStream(
          Paths.get(resourcesDir, propertyFile))) {
        Properties binaryDataProperties = new Properties();
        binaryDataProperties.load(propertiesStream);
        Map<String, String> binaryDataInfo = binaryDataProperties.entrySet().stream().collect(
            HashMap::new, (map, entry) -> map.put(String.valueOf(entry.getKey()),
                String.valueOf(entry.getValue())
            ), HashMap::putAll);
        details.put(BINARY_DATA_KEY, binaryDataInfo);
      } catch (IOException ex) {
        throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, ex.getMessage());
      }
    }
  }

  private void updateDescription(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    details.put(DESCRIPTION_KEY, PLUGIN_DESCRIPTION);
  }

  private void updateMetadata(IntegrationType integrationType) {
    Map<String, Object> details = integrationType.getDetails().getDetails();
    details.put(METADATA_KEY, PLUGIN_METADATA);
  }
}
