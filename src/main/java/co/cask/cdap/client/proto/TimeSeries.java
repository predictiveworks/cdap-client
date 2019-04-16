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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeSeries {

	private String metricName;
	private Map<String, String> grouping;

	private List<TimeValue> data;

	@SuppressWarnings("unchecked")
	public TimeSeries(Map<String, Object> record) {

		this.metricName = (String) record.get("metricName");
		this.grouping = (Map<String, String>) record.get("grouping");

		List<Map<String, Object>> data = (List<Map<String, Object>>) record.get("data");

		this.data = new ArrayList<TimeValue>();
		for (Map<String, Object> item : data) {
			this.data.add(new TimeValue(item));
		}
	}

	public String getMetricName() {
		return metricName;
	}

	public Map<String, String> getGrouping() {
		return grouping;
	}

	public List<TimeValue> getData() {
		return data;
	}

}
