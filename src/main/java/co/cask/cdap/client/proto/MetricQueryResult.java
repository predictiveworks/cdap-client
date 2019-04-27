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

/**
 * Represents metric query result. This is used for decorating REST API output.
 */
public class MetricQueryResult {

	private Double startTime;
	private Double endTime;
	private String resolution;

	private List<TimeSeries> series;

	public MetricQueryResult(Map<String, Object> result) {

		this.startTime = (Double) result.get("startTime");
		this.endTime = (Double) result.get("endTime");

		String resolution = (String) result.get("resolution");
		this.resolution = String.valueOf(resolution);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> seriesList = (List<Map<String, Object>>) result.get("series");

		this.series = new ArrayList<TimeSeries>();
		for (Map<String, Object> series : seriesList) {
			this.series.add(new TimeSeries(series));
		}

	}

	public long getStartTime() {
		return startTime.longValue();
	}

	public long getEndTime() {
		return endTime.longValue();
	}

	public List<TimeSeries> getSeries() {
		return series;
	}

	public String getResolution() {
		return resolution;
	}
}
