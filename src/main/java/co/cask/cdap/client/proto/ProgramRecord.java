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

import co.cask.cdap.proto.ProgramType;

/**
 * Represents a program in an HTTP response.
 */
public class ProgramRecord {
	private final ProgramType type;
	private final String app;
	private final String name;
	private final String description;

	public ProgramRecord(Map<String, Object> program) {

		this.type = ProgramType.valueOfPrettyName((String) program.get("type"));
		this.app = (String) program.get("app");
		;
		this.name = (String) program.get("name");
		this.description = (String) program.get("description");
	}

	public ProgramType getType() {
		return type;
	}

	public String getApp() {
		return app;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
