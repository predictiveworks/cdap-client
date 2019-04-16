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

public class TimeValue {

	private Double time;
	private Double value;

	public TimeValue(Map<String, Object> data) {

		this.time = (Double) data.get("time");
		this.value = (Double) data.get("value");
	}

	public long getTime() {
		return time.longValue();
	}

	public long getValue() {
		return value.longValue();
	}
}
