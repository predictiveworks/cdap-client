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

/**
 * Represents tag and its value associated with metrics data points.
 */
public class MetricTagValue {

	private String name;
	private String value;

	public MetricTagValue(Map<String, Object> data) {

		this.name = (String) data.get("name");

		this.value = null;
		if (data.containsKey("value"))
			this.value = (String) data.get("value");

	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MetricTagValue tagValue = (MetricTagValue) o;

		boolean result = value == null ? tagValue.value == null : value.equals(tagValue.value);
		return result && name.equals(tagValue.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode() + (value == null ? 0 : 31 * value.hashCode());
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("MetricTagValue");
		sb.append("{name='").append(name).append('\'');
		sb.append(", value='").append(value == null ? "null" : value).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
