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

public class PluginSummary {

	protected String name;
	protected String type;
	protected String description;
	protected String className;
	protected ArtifactSummary artifact;

	public PluginSummary(Map<String, Object> record) {

		this.name = (String) record.get("name");
		this.type = (String) record.get("type");

		this.description = (String) record.get("description");
		this.className = (String) record.get("className");

		@SuppressWarnings("unchecked")
		Map<String, Object> artifact = (Map<String, Object>) record.get("artifact");
		this.artifact = new ArtifactSummary(artifact);

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

	public String getClassName() {
		return className;
	}

	public ArtifactSummary getArtifact() {
		return artifact;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PluginSummary that = (PluginSummary) o;

		return Objects.equals(name, that.name) && Objects.equals(type, that.type)
				&& Objects.equals(description, that.description) && Objects.equals(className, that.className)
				&& Objects.equals(artifact, that.artifact);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, description, className, artifact);
	}

	@Override
	public String toString() {
		return "PluginSummary{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", description='" + description
				+ '\'' + ", className='" + className + '\'' + ", artifact=" + artifact + '}';
	}
}
