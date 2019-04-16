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
import javax.annotation.Nullable;

import co.cask.cdap.proto.Containers.ContainerType;

public class ContainerInfo {
	private String type;
	private String name;
	private Integer instance;
	private String container;
	private String host;
	private Integer memory;
	private Integer virtualCores;
	private Integer debugPort;

	public ContainerInfo(Map<String, Object> record) {

		this.type = (String) record.get("type");
		this.name = (String) record.get("name");

		this.container = null;
		if (record.containsKey("container")) {
			this.container = (String) record.get("container");
		}
		this.host = null;
		if (record.containsKey("host")) {
			this.host = (String) record.get("host");
		}
		/*
		 * HttpResponse transforms [Integer] into [Double]; we therefore have to extract
		 * the double value first
		 */
		this.instance = -1;
		if (record.containsKey("instance")) {
			Double instance = (Double) record.get("instance");
			this.instance = instance.intValue();
		}
		this.memory = -1;
		if (record.containsKey("memory")) {
			Double memory = (Double) record.get("memory");
			this.memory = memory.intValue();
		}

		this.virtualCores = -1;
		if (record.containsKey("virtualCores")) {
			Double virtualCores = (Double) record.get("virtualCores");
			this.virtualCores = virtualCores.intValue();
		}

		this.debugPort = -1;
		if (record.containsKey("debugPort")) {
			Double debugPort = (Double) record.get("debugPort");
			this.debugPort = debugPort.intValue();
		}

	}

	public ContainerInfo(ContainerType type, String name, @Nullable Integer instance, @Nullable String container,
			@Nullable String host, @Nullable Integer memory, @Nullable Integer virtualCores,
			@Nullable Integer debugPort) {
		this.type = type.name().toLowerCase();
		this.name = name;
		this.instance = instance;
		this.container = container;
		this.host = host;
		this.memory = memory;
		this.virtualCores = virtualCores;
		this.debugPort = debugPort;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ContainerInfo{");
		sb.append("type='").append(type).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", instance=").append(instance);
		sb.append(", container='").append(container).append('\'');
		sb.append(", host='").append(host).append('\'');
		sb.append(", memory=").append(memory);
		sb.append(", virtualCores=").append(virtualCores);
		sb.append(", debugPort=").append(debugPort);
		sb.append('}');
		return sb.toString();
	}

	public ContainerType getType() {
		return ContainerType.valueOf(type.toUpperCase());
	}

	public String getName() {
		return name;
	}

	@Nullable
	public Integer getInstance() {
		return instance;
	}

	@Nullable
	public String getContainer() {
		return container;
	}

	@Nullable
	public String getHost() {
		return host;
	}

	@Nullable
	public Integer getMemory() {
		return memory;
	}

	@Nullable
	public Integer getVirtualCores() {
		return virtualCores;
	}

	@Nullable
	public Integer getDebugPort() {
		return debugPort;
	}
}
