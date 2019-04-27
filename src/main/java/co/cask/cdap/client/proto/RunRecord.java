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

import co.cask.cdap.proto.ProgramRunCluster;
import co.cask.cdap.proto.id.ProfileId;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class records information for a particular run. The timestamp in the run
 * record has time unit SECONDS
 */
public class RunRecord {

	private String pid;
	private long startTs;
	private Long runTs = 0L;
	private Long stopTs = 0L;

	private Long suspendTs = 0L;
	private Long resumeTs = 0L;
	private String status;

	private Map<String, String> properties;

	private final ProgramRunCluster cluster;
	private final ProfileId profileId;

	@SuppressWarnings("unchecked")
	public RunRecord(Map<String, Object> record) {

		this.pid = (String) record.get("runid");
		this.startTs = ((Double) record.get("starting")).longValue();

		if (record.containsKey("start"))
			this.runTs = ((Double) record.get("start")).longValue();

		if (record.containsKey("end"))
			this.stopTs = ((Double) record.get("end")).longValue();

		if (record.containsKey("suspend"))
			this.suspendTs = ((Double) record.get("suspend")).longValue();

		if (record.containsKey("resume"))
			this.resumeTs = ((Double) record.get("resume")).longValue();

		this.status = (String) record.get("status");
		this.properties = (Map<String, String>) record.get("properties");
		this.properties = properties == null ? Collections.emptyMap()
				: Collections.unmodifiableMap(new LinkedHashMap<>(properties));

		/*
		 * The current version does not initialize 'cluster' and 'profile'
		 */
		this.cluster = null;
		this.profileId = null;

	}

	public String getPid() {
		return pid;
	}

	public long getStartTs() {
		return startTs;
	}

	public Long getRunTs() {
		return runTs;
	}

	public Long getStopTs() {
		return stopTs;
	}

	public Long getSuspendTs() {
		return suspendTs;
	}

	public Long getResumeTs() {
		return resumeTs;
	}

	public String getStatus() {
		return status;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public ProgramRunCluster getCluster() {
		return cluster;
	}

	public ProfileId getProfileId() {
		return profileId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RunRecord that = (RunRecord) o;

		return Objects.equals(this.pid, that.pid) && Objects.equals(this.startTs, that.startTs)
				&& Objects.equals(this.runTs, that.runTs) && Objects.equals(this.stopTs, that.stopTs)
				&& Objects.equals(this.suspendTs, that.suspendTs) && Objects.equals(this.resumeTs, that.resumeTs)
				&& Objects.equals(this.status, that.status) && Objects.equals(this.properties, that.properties)
				&& Objects.equals(this.cluster, that.cluster) && Objects.equals(this.profileId, that.profileId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pid, startTs, runTs, stopTs, suspendTs, resumeTs, status, properties, cluster, profileId);
	}

	@Override
	public String toString() {
		return "RunRecord{" + "pid='" + pid + '\'' + ", startTs=" + startTs + ", runTs=" + runTs + ", stopTs=" + stopTs
				+ ", suspendTs=" + suspendTs + ", resumeTs=" + resumeTs + ", status=" + status + ", properties="
				+ properties + ", cluster=" + cluster + ", profile=" + profileId + '}';

	}
}
