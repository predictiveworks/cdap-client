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
 * Contains information about a property used by a plugin.
 */
public class PluginPropertyField {

	private String name;
	private String description;
	private String type;
	private boolean required;
	private boolean macroSupported;
	private boolean macroEscapingEnabled;

	public PluginPropertyField(Map<String, Object> record) {

		this.name = (String) record.get("name");
		this.type = (String) record.get("type");

		this.description = (String) record.get("description");

		this.required = (Boolean) record.get("required");
		this.macroSupported = (Boolean) record.get("macroSupported");

		this.macroEscapingEnabled = (Boolean) record.get("macroEscapingEnabled");

	}

	/**
	 * Returns name of the property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns description for the property.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns {@code true} if the property is required by the plugin, {@code false}
	 * otherwise.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Returns {@code true} if the property supports macro, {@code false} otherwise.
	 */
	public boolean isMacroSupported() {
		return macroSupported;
	}

	/**
	 * Returns {@code true} if the macro escaping is enabled, {@code false}
	 * otherwise.
	 */
	public boolean isMacroEscapingEnabled() {
		return macroEscapingEnabled;
	}

	/**
	 * Returns the type of the property.
	 */
	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PluginPropertyField that = (PluginPropertyField) o;

		return required == that.required && name.equals(that.name) && description.equals(that.description)
				&& type.equals(that.type) && macroSupported == that.macroSupported
				&& macroEscapingEnabled == that.macroEscapingEnabled;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, type, required, macroSupported, macroEscapingEnabled);
	}
}
