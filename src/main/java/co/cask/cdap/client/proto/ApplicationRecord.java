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

package co.cask.cdap.client.proto;

import co.cask.cdap.client.proto.ArtifactSummary;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Represents item in the list from /apps
 */
public class ApplicationRecord {
	
	private final String type;
	private final String name;
	private final String version;
	private final String description;
	private final ArtifactSummary artifact;
	private final String ownerPrincipal;

	public ApplicationRecord(Map<String,Object> record) {

		@SuppressWarnings("unchecked")
		Map<String,Object> summary = (Map<String,Object>)record.get("artifact");
		this.artifact = new ArtifactSummary(summary);
		
		this.type = (String)record.get("type");
		this.name = (String)record.get("name");
		
		this.version = (String)record.get("version");
		this.description = (String)record.get("description");
		
		if (record.containsKey("principal"))
		  this.ownerPrincipal =(String)record.get("principal");
		
		else 
		  this.ownerPrincipal = null;
		
	}

	public ArtifactSummary getArtifact() {
		return artifact;
	}

	public String getAppVersion() {
		return version;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Nullable
	public String getOwnerPrincipal() {
		return ownerPrincipal;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ApplicationRecord that = (ApplicationRecord) o;

		return Objects.equals(type, that.type) && Objects.equals(name, that.name)
				&& Objects.equals(version, that.version) && Objects.equals(description, that.description)
				&& Objects.equals(artifact, that.artifact) && Objects.equals(ownerPrincipal, that.ownerPrincipal);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, version, description, artifact, ownerPrincipal);
	}

	@Override
	public String toString() {
		return "ApplicationRecord{" + "type='" + type + '\'' + ", name='" + name + '\'' + ", version='" + version + '\''
				+ ", description='" + description + '\'' + ", artifact=" + artifact + ", ownerPrincipal="
				+ ownerPrincipal + '}';
	}
}
