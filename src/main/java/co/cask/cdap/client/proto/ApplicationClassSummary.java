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

/**
 * Represents the objects returned by /classes/app
 */
public class ApplicationClassSummary {

	private final ArtifactSummary artifact;
	private final String className;

	public ApplicationClassSummary(Map<String, Object> record) {

		this.className = (String) record.get("className");
		@SuppressWarnings("unchecked")
		Map<String, Object> artifact = (Map<String, Object>) record.get("artifact");

		this.artifact = new ArtifactSummary(artifact);

	}

	public ApplicationClassSummary(ArtifactSummary artifact, String className) {
		this.artifact = artifact;
		this.className = className;
	}

	public ArtifactSummary getArtifact() {
		return artifact;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ApplicationClassSummary that = (ApplicationClassSummary) o;

		return Objects.equals(artifact, that.artifact) && Objects.equals(className, that.className);
	}

	@Override
	public int hashCode() {
		return Objects.hash(artifact, className);
	}

	@Override
	public String toString() {
		return "ApplicationClassSummary{" + "artifact=" + artifact + ", className='" + className + '\'' + '}';
	}
}
