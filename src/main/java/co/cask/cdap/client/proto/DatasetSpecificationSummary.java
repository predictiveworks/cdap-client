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

import java.util.Map;
import java.util.Objects;

public class DatasetSpecificationSummary {

	private String name;
	private String type;
	private String description;
	private Map<String, String> properties;

	@SuppressWarnings("unchecked")
	public DatasetSpecificationSummary(Map<String, Object> summary) {
		
		this.name = (String) summary.get("name");
		this.type = (String) summary.get("type");

		this.description = null;
		if (summary.containsKey("description"))
			this.description = (String) summary.get("description");

		this.properties = (Map<String, String>) summary.get("properties");

	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DatasetSpecificationSummary that = (DatasetSpecificationSummary) o;
		return Objects.equals(name, that.name) && Objects.equals(type, that.type)
				&& Objects.equals(description, that.description) && Objects.equals(properties, that.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, description, properties);
	}

	@Override
	public String toString() {
		return "DatasetSpecificationSummary{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", description='"
				+ description + '\'' + ", properties=" + properties + '}';
	}

}
