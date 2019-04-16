/*
 * Copyright © 2015 Cask Data, Inc.
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
package co.cask.cdap.client.config;

import co.cask.cdap.proto.id.NamespaceId;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.net.URI;
import javax.annotation.Nullable;

/**
 * Connection information to a CDAP instance.
 */
public class ConnectionConfig {

	private final String hostname;
	private final int port;
	private final boolean sslEnabled;

	public ConnectionConfig(ConnectionConfig config) {
		this(config.getHostname(), config.getPort(), config.isSSLEnabled());
	}

	public ConnectionConfig(String hostname, int port, boolean sslEnabled) {
		Preconditions.checkArgument(hostname != null && !hostname.isEmpty(), "hostname cannot be empty");
		this.hostname = hostname;
		this.port = port;
		this.sslEnabled = sslEnabled;
	}

	public URI getURI() {
		return URI.create(String.format("%s://%s:%d", sslEnabled ? "https" : "http", hostname, port));
	}

	public URI resolveURI(String path) {
		return getURI().resolve(String.format("/%s", path));
	}

	public URI resolveURI(String apiVersion, String path) {
		return getURI().resolve(String.format("/%s/%s", apiVersion, path));
	}

	public URI resolveNamespacedURI(NamespaceId namespace, String apiVersion, String path) {
		return getURI().resolve(String.format("/%s/namespaces/%s/%s", apiVersion, namespace.getNamespace(), path));
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public boolean isSSLEnabled() {
		return sslEnabled;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(hostname, port, sslEnabled);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final ConnectionConfig other = (ConnectionConfig) obj;
		return Objects.equal(this.hostname, other.hostname) && Objects.equal(this.port, other.port)
				&& Objects.equal(this.sslEnabled, other.sslEnabled);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("hostname", hostname).add("port", port).add("sslEnabled", sslEnabled)
				.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(ConnectionConfig connectionConfig) {
		return new Builder(connectionConfig);
	}

	/**
	 * Builder for {@link ConnectionConfig}.
	 */
	public static class Builder {

		private String hostname = null;
		private Integer port = null;
		private boolean sslEnabled = false;

		public Builder() {
		}

		public Builder(ConnectionConfig connectionConfig) {
			this.hostname = connectionConfig.hostname;
			this.port = connectionConfig.port;
			this.sslEnabled = connectionConfig.sslEnabled;
		}

		public Builder setHostname(String hostname) {
			this.hostname = hostname;
			return this;
		}

		/**
		 * @param port
		 *            connection port - use null if you want to use the default non-SSL
		 *            or SSL port, depending on sslEnabled
		 * @return this
		 */
		public Builder setPort(@Nullable Integer port) {
			this.port = port;
			return this;
		}

		public Builder setSSLEnabled(boolean sslEnabled) {
			this.sslEnabled = sslEnabled;
			return this;
		}

		public ConnectionConfig build() {
			return new ConnectionConfig(hostname, port, sslEnabled);
		}
	}
}
