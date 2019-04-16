/*
 * Copyright © 2015-2018 Cask Data, Inc.
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

package co.cask.cdap.client;

import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.api.artifact.ArtifactInfo;
import co.cask.cdap.api.artifact.ArtifactRange;
import co.cask.cdap.api.artifact.ArtifactScope;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.plugin.PluginClass;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.util.RESTClient;
/*
 * Changed to project internal exception to reduce dependencies
 * Dr. Krusche & Partner PartG
 */
import co.cask.cdap.client.common.ArtifactAlreadyExistsException;
import co.cask.cdap.client.common.ArtifactNotFoundException;
import co.cask.cdap.client.common.ArtifactRangeNotFoundException;
import co.cask.cdap.client.common.BadRequestException;
import co.cask.cdap.client.common.NotFoundException;
import co.cask.cdap.client.common.UnauthenticatedException;
import co.cask.cdap.client.proto.ApplicationClassSummary;
import co.cask.cdap.client.proto.ArtifactSummary;
import co.cask.cdap.internal.io.SchemaTypeAdapter;
import co.cask.cdap.proto.artifact.ApplicationClassInfo;
import co.cask.cdap.proto.artifact.PluginInfo;
import co.cask.cdap.proto.artifact.PluginSummary;
import co.cask.cdap.proto.id.ArtifactId;
import co.cask.cdap.proto.id.NamespaceId;
/*
 * Changed to project internal exception to reduce dependencies
 * Dr. Krusche & Partner PartG
 */
import co.cask.cdap.client.exception.UnauthorizedException;
import co.cask.common.ContentProvider;
import co.cask.common.http.HttpMethod;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpResponse;
import co.cask.common.http.ObjectResponse;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Provides ways to interact with CDAP Artifacts.
 */
@Beta
public class ArtifactClient {

	// TODO The subsequent approach to deserialize HTTP response is not working
	
	private static final Type APPCLASS_INFOS_TYPE = new TypeToken<List<ApplicationClassInfo>>() {
		private static final long serialVersionUID = 6865669976410227162L;
	}.getType();

	private static final Type EXTENSIONS_TYPE = new TypeToken<List<String>>() {
		private static final long serialVersionUID = -991592998848417037L;
	}.getType();

	private static final Type PLUGIN_SUMMARIES_TYPE = new TypeToken<List<PluginSummary>>() {
		private static final long serialVersionUID = -7736564386322893574L;
	}.getType();

	private static final Type PLUGIN_INFOS_TYPE = new TypeToken<List<PluginInfo>>() {

		private static final long serialVersionUID = 8932079376334126620L;
	}.getType();

	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Schema.class, new SchemaTypeAdapter())
			.create();

	private final RESTClient restClient;
	private final ClientConfig config;

	@Inject
	public ArtifactClient(ClientConfig config, RESTClient restClient) {
		this.config = config;
		this.restClient = restClient;
	}

	public ArtifactClient(ClientConfig config) {
		this(config, new RESTClient(config));
	}

	/**
	 * Lists all artifacts in the given namespace, including all system artifacts.
	 *
	 * @param namespace
	 *            the namespace to list artifacts in
	 * @return list of {@link ArtifactSummary}
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws NotFoundException
	 *             if the namespace could not be found
	 */
	public List<ArtifactSummary> list(NamespaceId namespace)
			throws IOException, UnauthenticatedException, NotFoundException, UnauthorizedException {
		return list(namespace, null);
	}

	/**
	 * Lists all artifacts in the given namespace, optionally including system
	 * artifacts.
	 *
	 * @param namespace
	 *            the namespace to list artifacts in
	 * @param scope
	 *            the scope of the artifacts to get. If {@code null}, both user and
	 *            system artifacts are listed
	 * @return list of {@link ArtifactSummary}
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws NotFoundException
	 *             if the namespace could not be found
	 */
	public List<ArtifactSummary> list(NamespaceId namespace, @Nullable ArtifactScope scope)
			throws IOException, UnauthenticatedException, NotFoundException, UnauthorizedException {

		URL url = scope == null ? config.resolveNamespacedURLV3(namespace, "artifacts")
				: config.resolveNamespacedURLV3(namespace, String.format("artifacts?scope=%s", scope.name()));
		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);

		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundException(namespace);
		}
		/*
		 * Extraction of response body changed, as initial 
		 * implementation has an issue with Gson
		 */
		List<Map<String,Object>> records = ObjectResponse.fromJsonBody(response, new TypeToken<List<Map<String, Object>>>() {
			private static final long serialVersionUID = -8431723712928585489L;
		}).getResponseObject();
		
		List<ArtifactSummary> result = new ArrayList<ArtifactSummary>();
		for (Map<String,Object> record:records) {
			result.add(new ArtifactSummary(record));
		}

		return result;
	}

	/**
	 * Lists all versions of the given artifact in the given namespace.
	 *
	 * @param namespace
	 *            the namespace to list artifact versions in
	 * @param artifactName
	 *            the name of the artifact
	 * @return list of {@link ArtifactSummary}
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 */
	public List<ArtifactSummary> listVersions(NamespaceId namespace, String artifactName)
			throws UnauthenticatedException, IOException, ArtifactNotFoundException, UnauthorizedException {
		return listVersions(namespace, artifactName, null);
	}

	/**
	 * Lists all versions of the given artifact in the given namespace.
	 *
	 * @param namespace
	 *            the namespace to list artifact versions in
	 * @param artifactName
	 *            the name of the artifact
	 * @param scope
	 *            the scope of artifacts to get. If none is given, the scope
	 *            defaults to the user scope
	 * @return list of {@link ArtifactSummary}
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 */
	public List<ArtifactSummary> listVersions(NamespaceId namespace, String artifactName, @Nullable ArtifactScope scope)
			throws UnauthenticatedException, IOException, ArtifactNotFoundException, UnauthorizedException {

		URL url = scope == null ? config.resolveNamespacedURLV3(namespace, String.format("artifacts/%s", artifactName))
				: config.resolveNamespacedURLV3(namespace,
						String.format("artifacts/%s?scope=%s", artifactName, scope.name()));

		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(namespace, artifactName);
		}
		/*
		 * Extraction of response body changed, as initial 
		 * implementation has an issue with Gson
		 */
		List<Map<String,Object>> records = ObjectResponse.fromJsonBody(response, new TypeToken<List<Map<String, Object>>>() {
			private static final long serialVersionUID = -8431723712928585489L;
		}).getResponseObject();
		
		List<ArtifactSummary> result = new ArrayList<ArtifactSummary>();
		for (Map<String,Object> record:records) {
			result.add(new ArtifactSummary(record));
		}

		return result;
	}

	/**
	 * Gets information about a specific artifact version.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @return information about the given artifact
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 */
	public ArtifactInfo getArtifactInfo(ArtifactId artifactId)
			throws IOException, UnauthenticatedException, ArtifactNotFoundException, UnauthorizedException {
		ArtifactInfo info;
		try {
			info = getArtifactInfo(artifactId, ArtifactScope.SYSTEM);
		} catch (ArtifactNotFoundException e) {
			info = getArtifactInfo(artifactId, ArtifactScope.USER);
		}
		return info;
	}

	/**
	 * Gets information about a specific artifact version.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @param scope
	 *            the scope of the artifact
	 * @return information about the given artifact
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 */
	public ArtifactInfo getArtifactInfo(ArtifactId artifactId, ArtifactScope scope)
			throws IOException, UnauthenticatedException, ArtifactNotFoundException, UnauthorizedException {

		String path = String.format("artifacts/%s/versions/%s?scope=%s", artifactId.getArtifact(),
				artifactId.getVersion(), scope.name());

		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);
		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(artifactId);
		}
		return ObjectResponse.fromJsonBody(response, ArtifactInfo.class, GSON).getResponseObject();
	}

	/**
	 * Get summaries of all application classes in the given namespace, including
	 * classes from system artifacts.
	 *
	 * @param namespace
	 *            the namespace to list application classes from
	 * @return summaries of all application classes in the given namespace,
	 *         including classes from system artifacts
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ApplicationClassSummary> getApplicationClasses(NamespaceId namespace)
			throws IOException, UnauthenticatedException, UnauthorizedException {
		return getApplicationClasses(namespace, (ArtifactScope) null);
	}

	/**
	 * Get summaries of all application classes in the given namespace, optionally
	 * including classes from system artifacts.
	 *
	 * @param namespace
	 *            the namespace to list application classes from
	 * @param scope
	 *            the scope to list application classes in. If null, classes from
	 *            all scopes are returned
	 * @return summaries of all application classes in the given namespace,
	 *         including classes from system artifacts
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ApplicationClassSummary> getApplicationClasses(NamespaceId namespace, @Nullable ArtifactScope scope)
			throws IOException, UnauthenticatedException, UnauthorizedException {

		String path = scope == null ? "classes/apps" : String.format("classes/apps?scope=%s", scope.name());
		URL url = config.resolveNamespacedURLV3(namespace, path);

		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
		/*
		 * Extraction of response body changed, as initial 
		 * implementation has an issue with Gson
		 */
		List<Map<String,Object>> records = ObjectResponse.fromJsonBody(response, new TypeToken<List<Map<String, Object>>>() {
			private static final long serialVersionUID = -8431723712928585489L;
		}).getResponseObject();
		
		List<ApplicationClassSummary> result = new ArrayList<ApplicationClassSummary>();
		for (Map<String,Object> record:records) {
			result.add(new ApplicationClassSummary(record));
		}

		return result;
		
	}

	/**
	 * Get information about all application classes in the specified namespace, of
	 * the specified class name.
	 *
	 * @param namespace
	 *            the namespace to list application classes from
	 * @return summaries of all application classes in the given namespace,
	 *         including classes from system artifacts
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ApplicationClassInfo> getApplicationClasses(NamespaceId namespace, String className)
			throws IOException, UnauthenticatedException, UnauthorizedException {
		return getApplicationClasses(namespace, className, ArtifactScope.USER);
	}

	/**
	 * Get information about all application classes in the specified namespace, of
	 * the specified class name.
	 *
	 * @param namespace
	 *            the namespace to list application classes from
	 * @param scope
	 *            the scope to list application classes in
	 * @return summaries of all application classes in the given namespace,
	 *         including classes from system artifacts
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ApplicationClassInfo> getApplicationClasses(NamespaceId namespace, String className,
			ArtifactScope scope) throws IOException, UnauthenticatedException, UnauthorizedException {

		String path = String.format("classes/apps/%s?scope=%s", className, scope.name());
		URL url = config.resolveNamespacedURLV3(namespace, path);

		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
		return ObjectResponse.<List<ApplicationClassInfo>>fromJsonBody(response, APPCLASS_INFOS_TYPE, GSON)
				.getResponseObject();
	}

	/**
	 * Gets all the plugin types available to a specific artifact.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @return list of plugin types available to the given artifact.
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<String> getPluginTypes(ArtifactId artifactId)
			throws IOException, UnauthenticatedException, ArtifactNotFoundException, UnauthorizedException {
		List<String> pluginTypes;
		try {
			pluginTypes = getPluginTypes(artifactId, ArtifactScope.SYSTEM);
		} catch (ArtifactNotFoundException e) {
			pluginTypes = getPluginTypes(artifactId, ArtifactScope.USER);
		}
		return pluginTypes;
	}

	/**
	 * Gets all the plugin types available to a specific artifact.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @param scope
	 *            the scope of the artifact
	 * @return list of plugin types available to the given artifact.
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<String> getPluginTypes(ArtifactId artifactId, ArtifactScope scope)
			throws IOException, UnauthenticatedException, ArtifactNotFoundException, UnauthorizedException {

		String path = String.format("artifacts/%s/versions/%s/extensions?scope=%s", artifactId.getArtifact(),
				artifactId.getVersion(), scope.name());
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);

		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(artifactId);
		}
		return ObjectResponse.<List<String>>fromJsonBody(response, EXTENSIONS_TYPE).getResponseObject();
	}

	/**
	 * Gets all the plugins of the given type available to the given artifact.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @param pluginType
	 *            the type of plugins to get
	 * @return list of {@link PluginSummary}
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<PluginSummary> getPluginSummaries(ArtifactId artifactId, String pluginType)
			throws IOException, UnauthenticatedException, ArtifactNotFoundException, UnauthorizedException {
		List<PluginSummary> pluginSummary;
		try {
			pluginSummary = getPluginSummaries(artifactId, pluginType, ArtifactScope.SYSTEM);
		} catch (ArtifactNotFoundException e) {
			pluginSummary = getPluginSummaries(artifactId, pluginType, ArtifactScope.USER);
		}
		return pluginSummary;
	}

	/**
	 * Gets all the plugins of the given type available to the given artifact.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @param pluginType
	 *            the type of plugins to get
	 * @param scope
	 *            the scope of the artifact
	 * @return list of {@link PluginSummary}
	 * @throws ArtifactNotFoundException
	 *             if the given artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<PluginSummary> getPluginSummaries(ArtifactId artifactId, String pluginType, ArtifactScope scope)
			throws IOException, UnauthenticatedException, ArtifactNotFoundException, UnauthorizedException {

		String path = String.format("artifacts/%s/versions/%s/extensions/%s?scope=%s", artifactId.getArtifact(),
				artifactId.getVersion(), pluginType, scope.name());
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);

		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(artifactId);
		}
		return ObjectResponse.<List<PluginSummary>>fromJsonBody(response, PLUGIN_SUMMARIES_TYPE).getResponseObject();
	}

	/**
	 * Gets all the plugins of the given type and name available to the given
	 * artifact.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @param pluginType
	 *            the type of plugins to get
	 * @param pluginName
	 *            the name of the plugins to get
	 * @return list of {@link PluginInfo}
	 * @throws NotFoundException
	 *             if the given artifact does not exist or plugins for that artifact
	 *             do not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<PluginInfo> getPluginInfo(ArtifactId artifactId, String pluginType, String pluginName)
			throws IOException, UnauthenticatedException, NotFoundException, UnauthorizedException {
		List<PluginInfo> pluginInfo;
		try {
			pluginInfo = getPluginInfo(artifactId, pluginType, pluginName, ArtifactScope.SYSTEM);
		} catch (NotFoundException e) {
			pluginInfo = getPluginInfo(artifactId, pluginType, pluginName, ArtifactScope.USER);
		}
		return pluginInfo;
	}

	/**
	 * Gets all the plugins of the given type and name available to the given
	 * artifact.
	 *
	 * @param artifactId
	 *            the id of the artifact to get
	 * @param pluginType
	 *            the type of plugins to get
	 * @param pluginName
	 *            the name of the plugins to get
	 * @param scope
	 *            the scope of the artifact
	 * @return list of {@link PluginInfo}
	 * @throws NotFoundException
	 *             if the given artifact does not exist or plugins for that artifact
	 *             do not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<PluginInfo> getPluginInfo(ArtifactId artifactId, String pluginType, String pluginName,
			ArtifactScope scope)
			throws IOException, UnauthenticatedException, NotFoundException, UnauthorizedException {

		String path = String.format("artifacts/%s/versions/%s/extensions/%s/plugins/%s?scope=%s",
				artifactId.getArtifact(), artifactId.getVersion(), pluginType, pluginName, scope.name());
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);

		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundException(response.getResponseBodyAsString());
		}
		return ObjectResponse.<List<PluginInfo>>fromJsonBody(response, PLUGIN_INFOS_TYPE).getResponseObject();
	}

	/**
	 * Add an artifact.
	 *
	 * @param artifactId
	 *            the id of the artifact to add
	 * @param parentArtifacts
	 *            the set of artifacts this artifact extends
	 * @param artifactContents
	 *            an input supplier for the contents of the artifact
	 * @throws ArtifactAlreadyExistsException
	 *             if the artifact already exists
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws ArtifactRangeNotFoundException
	 *             if the parent artifacts do not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void add(ArtifactId artifactId, @Nullable Set<ArtifactRange> parentArtifacts,
			ContentProvider<? extends InputStream> artifactContents)
			throws UnauthenticatedException, BadRequestException, ArtifactRangeNotFoundException,
			ArtifactAlreadyExistsException, IOException, UnauthorizedException {

		add(artifactId.getParent(), artifactId.getArtifact(), artifactContents, artifactId.getVersion(),
				parentArtifacts);
	}

	/**
	 * Add an artifact.
	 *
	 * @param namespace
	 *            the namespace to add the artifact to
	 * @param artifactName
	 *            the name of the artifact to add
	 * @param artifactContents
	 *            a provider for the contents of the artifact
	 * @param artifactVersion
	 *            the version of the artifact to add. If null, the version will be
	 *            derived from the manifest of the artifact.
	 * @throws ArtifactAlreadyExistsException
	 *             if the artifact already exists
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws ArtifactRangeNotFoundException
	 *             if the parent artifacts do not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void add(NamespaceId namespace, String artifactName, ContentProvider<? extends InputStream> artifactContents,
			@Nullable String artifactVersion) throws ArtifactAlreadyExistsException, BadRequestException, IOException,
			UnauthenticatedException, ArtifactRangeNotFoundException, UnauthorizedException {

		add(namespace, artifactName, artifactContents, artifactVersion, null, null);
	}

	/**
	 * Add an artifact.
	 *
	 * @param namespace
	 *            the namespace to add the artifact to
	 * @param artifactName
	 *            the name of the artifact to add
	 * @param artifactContents
	 *            a provider for the contents of the artifact
	 * @param artifactVersion
	 *            the version of the artifact to add. If null, the version will be
	 *            derived from the manifest of the artifact
	 * @param parentArtifacts
	 *            the set of artifacts this artifact extends
	 * @throws ArtifactAlreadyExistsException
	 *             if the artifact already exists
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws ArtifactRangeNotFoundException
	 *             if the parent artifacts do not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void add(NamespaceId namespace, String artifactName, ContentProvider<? extends InputStream> artifactContents,
			@Nullable String artifactVersion, @Nullable Set<ArtifactRange> parentArtifacts)
			throws ArtifactAlreadyExistsException, BadRequestException, IOException, UnauthenticatedException,
			ArtifactRangeNotFoundException, UnauthorizedException {

		add(namespace, artifactName, artifactContents, artifactVersion, parentArtifacts, null);
	}

	/**
	 * Add an artifact.
	 *
	 * @param namespace
	 *            the namespace to add the artifact to
	 * @param artifactName
	 *            the name of the artifact to add
	 * @param artifactContents
	 *            a provider for the contents of the artifact
	 * @param artifactVersion
	 *            the version of the artifact to add. If null, the version will be
	 *            derived from the manifest of the artifact
	 * @param parentArtifacts
	 *            the set of artifacts this artifact extends
	 * @param additionalPlugins
	 *            the set of plugins contained in the artifact that cannot be
	 *            determined through jar inspection. This set should include any
	 *            classes that are plugins but could not be annotated as such. For
	 *            example, 3rd party classes like jdbc drivers fall into this
	 *            category.
	 * @throws ArtifactAlreadyExistsException
	 *             if the artifact already exists
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws ArtifactRangeNotFoundException
	 *             if the parent artifacts do not exist
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void add(NamespaceId namespace, String artifactName, ContentProvider<? extends InputStream> artifactContents,
			@Nullable String artifactVersion, @Nullable Set<ArtifactRange> parentArtifacts,
			@Nullable Set<PluginClass> additionalPlugins) throws ArtifactAlreadyExistsException, BadRequestException,
			IOException, UnauthenticatedException, ArtifactRangeNotFoundException, UnauthorizedException {

		URL url = config.resolveNamespacedURLV3(namespace, String.format("artifacts/%s", artifactName));
		HttpRequest.Builder requestBuilder = HttpRequest.post(url);
		if (artifactVersion != null) {
			requestBuilder.addHeader("Artifact-Version", artifactVersion);
		}
		if (parentArtifacts != null && !parentArtifacts.isEmpty()) {
			requestBuilder.addHeader("Artifact-Extends", Joiner.on('/').join(parentArtifacts));
		}
		if (additionalPlugins != null && !additionalPlugins.isEmpty()) {
			requestBuilder.addHeader("Artifact-Plugins", GSON.toJson(additionalPlugins));
		}
		HttpRequest request = requestBuilder.withBody(artifactContents).build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_CONFLICT,
				HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_NOT_FOUND);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
			throw new ArtifactAlreadyExistsException(response.getResponseBodyAsString());
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(response.getResponseBodyAsString());
		} else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactRangeNotFoundException(parentArtifacts);
		}
	}

	/**
	 * Delete an artifact.
	 *
	 * @param artifactId
	 *            the artifact to delete
	 *
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void delete(ArtifactId artifactId)
			throws IOException, UnauthenticatedException, BadRequestException, UnauthorizedException {
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(),
				String.format("artifacts/%s/versions/%s", artifactId.getArtifact(), artifactId.getVersion()));

		HttpRequest request = HttpRequest.delete(url).build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(),
				HttpURLConnection.HTTP_BAD_REQUEST);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(response.getResponseBodyAsString());
		}
	}

	/**
	 * Write properties for an artifact. Any existing properties will be
	 * overwritten.
	 *
	 * @param artifactId
	 *            the artifact to add properties to
	 * @param properties
	 *            the properties to add
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 */
	public void writeProperties(ArtifactId artifactId, Map<String, String> properties) throws IOException,
			UnauthenticatedException, ArtifactNotFoundException, BadRequestException, UnauthorizedException {
		String path = String.format("artifacts/%s/versions/%s/properties", artifactId.getArtifact(),
				artifactId.getVersion());
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);
		HttpRequest.Builder requestBuilder = HttpRequest.put(url);
		HttpRequest request = requestBuilder.withBody(GSON.toJson(properties)).build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_BAD_REQUEST,
				HttpURLConnection.HTTP_NOT_FOUND);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(artifactId);
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(response.getResponseBodyAsString());
		}
	}

	/**
	 * Delete all properties for an artifact. If no properties exist, this will be a
	 * no-op.
	 *
	 * @param artifactId
	 *            the artifact to delete properties from
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 */
	public void deleteProperties(ArtifactId artifactId) throws IOException, UnauthenticatedException,
			ArtifactNotFoundException, BadRequestException, UnauthorizedException {
		String path = String.format("artifacts/%s/versions/%s/properties", artifactId.getArtifact(),
				artifactId.getVersion());
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);
		HttpRequest.Builder requestBuilder = HttpRequest.delete(url);
		HttpRequest request = requestBuilder.build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_BAD_REQUEST,
				HttpURLConnection.HTTP_NOT_FOUND);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(artifactId);
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(response.getResponseBodyAsString());
		}
	}

	/**
	 * Write a property for an artifact. If the property already exists, it will be
	 * overwritten. If the property does not exist, it will be added.
	 *
	 * @param artifactId
	 *            the artifact to write the property to
	 * @param key
	 *            the property key to write
	 * @param value
	 *            the property value to write
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 */
	public void writeProperty(ArtifactId artifactId, String key, String value) throws IOException,
			UnauthenticatedException, ArtifactNotFoundException, BadRequestException, UnauthorizedException {
		String path = String.format("artifacts/%s/versions/%s/properties/%s", artifactId.getArtifact(),
				artifactId.getVersion(), key);
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);
		HttpRequest.Builder requestBuilder = HttpRequest.put(url);
		HttpRequest request = requestBuilder.withBody(value).build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_BAD_REQUEST,
				HttpURLConnection.HTTP_NOT_FOUND);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(artifactId);
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(response.getResponseBodyAsString());
		}
	}

	/**
	 * Delete a property for an artifact. If the property does not exist, this will
	 * be a no-op.
	 *
	 * @param artifactId
	 *            the artifact to delete a property from
	 * @param key
	 *            the property to delete
	 * @throws BadRequestException
	 *             if the request is invalid. For example, if the artifact name or
	 *             version is invalid
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws ArtifactNotFoundException
	 *             if the artifact does not exist
	 * @throws IOException
	 *             if a network error occurred
	 */
	public void deleteProperty(ArtifactId artifactId, String key) throws IOException, UnauthenticatedException,
			ArtifactNotFoundException, BadRequestException, UnauthorizedException {
		String path = String.format("artifacts/%s/versions/%s/properties/%s", artifactId.getArtifact(),
				artifactId.getVersion(), key);
		URL url = config.resolveNamespacedURLV3(artifactId.getParent(), path);
		HttpRequest.Builder requestBuilder = HttpRequest.delete(url);
		HttpRequest request = requestBuilder.build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_BAD_REQUEST,
				HttpURLConnection.HTTP_NOT_FOUND);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ArtifactNotFoundException(artifactId);
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(response.getResponseBodyAsString());
		}
	}
}
