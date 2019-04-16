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

import co.cask.cdap.client.proto.ArtifactSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Represents an application returned for /apps/{app-id}.
 */
public class ApplicationDetail {
	private final String name;
	private final String appVersion;
	private final String artifactVersion;
	private final String description;
	private final String configuration;
	private final List<StreamDetail> streams;
	private final List<DatasetDetail> datasets;
	private final List<ProgramRecord> programs;
	private final List<PluginDetail> plugins;
	private final ArtifactSummary artifact;
	private final String ownerPrincipal;

	public ApplicationDetail(Map<String, Object> detail) {

		@SuppressWarnings("unchecked")
		Map<String,Object> summary = (Map<String,Object>)detail.get("artifact");
		this.artifact = new ArtifactSummary(summary);

		this.name = (String) detail.get("name");
		this.appVersion = (String) detail.get("appVersion");

		this.artifactVersion = (String) detail.get("artifactVersion");
		this.description = (String) detail.get("description");

		if (detail.containsKey("principal"))
			this.ownerPrincipal = (String) detail.get("principal");

		else
			this.ownerPrincipal = null;

		this.configuration = (String) detail.get("configuration");

		/* Datasets */
		this.datasets = new ArrayList<DatasetDetail>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> datasets = (List<Map<String, Object>>) detail.get("datasets");
		for (Map<String, Object> dataset : datasets) {
			this.datasets.add(new DatasetDetail(dataset));
		}
		;

		/* Plugins */
		this.plugins = new ArrayList<PluginDetail>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> plugins = (List<Map<String, Object>>) detail.get("plugins");
		for (Map<String, Object> plugin : plugins) {
			this.plugins.add(new PluginDetail(plugin));

		}
		;

		/* Programs */
		this.programs = new ArrayList<ProgramRecord>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> programs = (List<Map<String, Object>>) detail.get("programs");
		for (Map<String, Object> program : programs) {
			this.programs.add(new ProgramRecord(program));
		}
		;

		/* Streams */
		this.streams = new ArrayList<StreamDetail>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> streams = (List<Map<String, Object>>) detail.get("streams");
		for (Map<String, Object> stream : streams) {
			this.streams.add(new StreamDetail(stream));
		}
		;

	}

	public String getArtifactVersion() {
		return this.artifactVersion;
	}

	public String getName() {
		return name;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getDescription() {
		return description;
	}

	public String getConfiguration() {
		return configuration;
	}

	public List<StreamDetail> getStreams() {
		return streams;
	}

	public List<DatasetDetail> getDatasets() {
		return datasets;
	}

	public List<PluginDetail> getPlugins() {
		return plugins;
	}

	public List<ProgramRecord> getPrograms() {
		return programs;
	}

	public ArtifactSummary getArtifact() {
		return artifact;
	}

	@Nullable
	public String getOwnerPrincipal() {
		return ownerPrincipal;
	}
}
