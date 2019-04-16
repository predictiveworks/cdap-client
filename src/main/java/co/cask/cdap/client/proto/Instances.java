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

public class Instances {

	private final int instances;

	public Instances(Map<String,Object> record) {
		/*
		 * HttpResponse transforms [Integer] into [Double]; we therefore
		 * have to extract the double value first 
		 */
		Double instances = (Double)record.get("instances");
		this.instances = instances.intValue();
		
	}
	
	public Instances(int instances) {
		this.instances = instances;
	}

	public int getInstances() {
		return instances;
	}

}
