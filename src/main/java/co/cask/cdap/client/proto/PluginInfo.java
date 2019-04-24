package co.cask.cdap.client.proto;
/*
 * Copyright 2019, Dr. Krusche & Partner PartG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Represents an plugin info returned by
 * /artifacts/{artifact-name}/versions/{artifact-version}/extensions/{plugin-type}/plugins/{plugin-name}
 */
public class PluginInfo extends PluginSummary {

	private String configFieldName;
	private Map<String, PluginPropertyField> properties;
	private Set<String> endpoints;

	@SuppressWarnings("unchecked")
	public PluginInfo(Map<String, Object> record) {
		super(record);

		this.configFieldName = null;
		if (record.containsKey("configFieldName"))
			this.configFieldName = (String) record.get("configFieldName");

		this.endpoints = (Set<String>) record.get("endpoints");

		this.properties = new HashMap<String, PluginPropertyField>();

		Map<String, Object> properties = (Map<String, Object>) record.get("properties");
		for (String key : properties.keySet()) {

			Map<String, Object> val = (Map<String, Object>) properties.get(key);
			this.properties.put(key, new PluginPropertyField(val));

		}

	}

	@Nullable
	public String getConfigFieldName() {
		return configFieldName;
	}

	public Map<String, PluginPropertyField> getProperties() {
		return properties;
	}

	public Set<String> getEndpoints() {
		return endpoints;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PluginInfo that = (PluginInfo) o;

		return super.equals(that) && Objects.equals(configFieldName, that.configFieldName)
				&& Objects.equals(properties, that.properties) && Objects.equals(endpoints, that.endpoints);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), configFieldName, properties, endpoints);
	}

	@Override
	public String toString() {
		return "PluginInfo{" + "configFieldName='" + configFieldName + '\'' + ", properties=" + properties
				+ ", endpoints=" + endpoints + ", name='" + name + '\'' + ", type='" + type + '\'' + ", description='"
				+ description + '\'' + ", className='" + className + '\'' + ", artifact=" + artifact + ", name='"
				+ getName() + '\'' + ", type='" + getType() + '\'' + ", description='" + getDescription() + '\''
				+ ", className='" + getClassName() + '\'' + ", artifact=" + getArtifact() + '}';
	}
}
