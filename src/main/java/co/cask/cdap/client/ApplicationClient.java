/*
 * Copyright © 2014-2018 Cask Data, Inc.
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

import co.cask.cdap.api.Config;
import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.util.RESTClient;
/*
 * Changed to project internal exception to reduce dependencies
 * Dr. Krusche & Partner PartG
 */
import co.cask.cdap.client.common.ApplicationNotFoundException;
import co.cask.cdap.client.common.BadRequestException;
import co.cask.cdap.client.common.NotFoundException;
import co.cask.cdap.client.common.UnauthenticatedException;
/*
 * Changed to project internal utils to reduce dependencies
 * Dr. Krusche & Partner PartG
 */
import co.cask.cdap.client.common.utils.Tasks;
import co.cask.cdap.client.proto.ApplicationDetail;
import co.cask.cdap.client.proto.ApplicationRecord;
import co.cask.cdap.client.proto.ProgramRecord;
import co.cask.cdap.proto.PluginInstanceDetail;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.ScheduleDetail;
import co.cask.cdap.proto.artifact.AppRequest;
import co.cask.cdap.proto.id.ApplicationId;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.proto.id.ScheduleId;
/*
 * Changed to project internal exception to reduce dependencies
 * Dr. Krusche & Partner PartG
 */
import co.cask.cdap.client.exception.UnauthorizedException;
import co.cask.common.http.HttpMethod;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpResponse;
import co.cask.common.http.ObjectResponse;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Provides ways to interact with CDAP applications.
 */
@Beta
public class ApplicationClient {

	private static final Gson GSON = new Gson();

	private final RESTClient restClient;
	private final ClientConfig config;

	@Inject
	public ApplicationClient(ClientConfig config, RESTClient restClient) {
		this.config = config;
		this.restClient = restClient;
	}

	public ApplicationClient(ClientConfig config) {
		this(config, new RESTClient(config));
	}

	/**
	 * Lists all applications currently deployed.
	 *
	 * @param namespace
	 *            the namespace to list applications from
	 * @return list of {@link ApplicationRecord ApplicationRecords}.
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ApplicationRecord> list(NamespaceId namespace)
			throws IOException, UnauthenticatedException, UnauthorizedException {
		HttpResponse response = restClient.execute(HttpMethod.GET, config.resolveNamespacedURLV3(namespace, "apps"),
				config.getAccessToken());
		/*
		 * Extraction of response body changed, as initial 
		 * implementation has an issue with Gson
		 */
		List<Map<String,Object>> apps = ObjectResponse.fromJsonBody(response, new TypeToken<List<Map<String, Object>>>() {
			private static final long serialVersionUID = 4278746096414716212L;
		}).getResponseObject();
		
		List<ApplicationRecord> records = new ArrayList<ApplicationRecord>();
		for (Map<String,Object> app: apps) {
			records.add(new ApplicationRecord(app));
		}
		
		return records;
	}

	/**
	 * Lists all applications currently deployed, optionally filtering to only
	 * include applications that use the specified artifact name and version.
	 *
	 * @param namespace
	 *            the namespace to list applications from
	 * @param artifactName
	 *            the name of the artifact to filter by. If null, no filtering will
	 *            be done.
	 * @param artifactVersion
	 *            the version of the artifact to filter by. If null, no filtering
	 *            will be done.
	 * @return list of {@link ApplicationRecord ApplicationRecords}.
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ApplicationRecord> list(NamespaceId namespace, @Nullable String artifactName,
			@Nullable String artifactVersion) throws IOException, UnauthenticatedException, UnauthorizedException {
		Set<String> names = new HashSet<>();
		if (artifactName != null) {
			names.add(artifactName);
		}
		return list(namespace, names, artifactVersion);
	}

	/**
	 * Lists all applications currently deployed, optionally filtering to only
	 * include applications that use one of the specified artifact names and the
	 * specified artifact version.
	 *
	 * @param namespace
	 *            the namespace to list applications from
	 * @param artifactNames
	 *            the set of artifact names to allow. If empty, no filtering will be
	 *            done.
	 * @param artifactVersion
	 *            the version of the artifact to filter by. If null, no filtering
	 *            will be done.
	 * @return list of {@link ApplicationRecord ApplicationRecords}.
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ApplicationRecord> list(NamespaceId namespace, Set<String> artifactNames,
			@Nullable String artifactVersion) throws IOException, UnauthenticatedException, UnauthorizedException {
		if (artifactNames.isEmpty() && artifactVersion == null) {
			return list(namespace);
		}

		String path;
		if (!artifactNames.isEmpty() && artifactVersion != null) {
			path = String.format("apps?artifactName=%s&artifactVersion=%s", Joiner.on(',').join(artifactNames),
					artifactVersion);
		} else if (!artifactNames.isEmpty()) {
			path = "apps?artifactName=" + Joiner.on(',').join(artifactNames);
		} else {
			path = "apps?artifactVersion=" + artifactVersion;
		}

		HttpResponse response = restClient.execute(HttpMethod.GET, config.resolveNamespacedURLV3(namespace, path),
				config.getAccessToken());
		/*
		 * Extraction of response body changed, as initial 
		 * implementation has an issue with Gson
		 */
		List<Map<String,Object>> apps = ObjectResponse.fromJsonBody(response, new TypeToken<List<Map<String, Object>>>() {
			private static final long serialVersionUID = 4278746096414716212L;
		}).getResponseObject();
		
		List<ApplicationRecord> records = new ArrayList<ApplicationRecord>();
		for (Map<String,Object> app: apps) {
			records.add(new ApplicationRecord(app));
		}
		
		return records;
	}

	/**
	 * Lists all applications currently deployed, optionally filtering to only
	 * include applications that use one of the specified artifact names and the
	 * specified artifact version.
	 *
	 * @param namespace
	 *            the namespace to list application versions from
	 * @param appName
	 *            the application name to list versions for
	 * @return list of {@link String} application versions.
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<String> listAppVersions(NamespaceId namespace, String appName)
			throws IOException, UnauthenticatedException, UnauthorizedException, ApplicationNotFoundException {
		String path = String.format("apps/%s/versions", appName);
		URL url = config.resolveNamespacedURLV3(namespace, path);
		HttpRequest request = HttpRequest.get(url).build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ApplicationNotFoundException(namespace.app(appName));
		}

		return ObjectResponse.fromJsonBody(response, new TypeToken<List<String>>() {
			private static final long serialVersionUID = -3335450794251092793L;
		}).getResponseObject();
	}

	/**
	 * Get details about the specified application.
	 *
	 * @param appId
	 *            the id of the application to get
	 * @return details about the specified application
	 * @throws ApplicationNotFoundException
	 *             if the application with the given ID was not found
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public ApplicationDetail get(ApplicationId appId)
			throws ApplicationNotFoundException, IOException, UnauthenticatedException, UnauthorizedException {

		String path = String.format("apps/%s/versions/%s", appId.getApplication(), appId.getVersion());
		HttpResponse response = restClient.execute(HttpMethod.GET,
				config.resolveNamespacedURLV3(appId.getParent(), path), config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ApplicationNotFoundException(appId);
		}
		/*
		 * Extraction of response body changed, as initial 
		 * implementation has an issue with Gson
		 */		
		Map<String,Object> detail = ObjectResponse.fromJsonBody(response, new TypeToken<Map<String, Object>>() {
			private static final long serialVersionUID = -3862658447421260348L;
		}).getResponseObject();
		
		return new ApplicationDetail(detail);
	}

	/**
	 * Get plugins in the specified application.
	 *
	 * @param appId
	 *            the id of the application to get
	 * @return list of plugins in the application
	 * @throws ApplicationNotFoundException
	 *             if the application with the given ID was not found
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<PluginInstanceDetail> getPlugins(ApplicationId appId)
			throws ApplicationNotFoundException, IOException, UnauthenticatedException, UnauthorizedException {

		HttpResponse response = restClient.execute(HttpMethod.GET,
				config.resolveNamespacedURLV3(appId.getParent(), "apps/" + appId.getApplication() + "/plugins"),
				config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ApplicationNotFoundException(appId);
		}
		return ObjectResponse.fromJsonBody(response, new TypeToken<List<PluginInstanceDetail>>() {
			private static final long serialVersionUID = -3862658447421260348L;
		}).getResponseObject();
	}

	/**
	 * Deletes an application.
	 *
	 * @param app
	 *            the application to delete
	 * @throws ApplicationNotFoundException
	 *             if the application with the given ID was not found
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void delete(ApplicationId app)
			throws ApplicationNotFoundException, IOException, UnauthenticatedException, UnauthorizedException {
		String path = String.format("apps/%s/versions/%s", app.getApplication(), app.getVersion());
		HttpResponse response = restClient.execute(HttpMethod.DELETE,
				config.resolveNamespacedURLV3(app.getParent(), path), config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ApplicationNotFoundException(app);
		}
	}

	/**
	 * Deletes all applications in a namespace.
	 *
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void deleteAll(NamespaceId namespace) throws IOException, UnauthenticatedException, UnauthorizedException {
		restClient.execute(HttpMethod.DELETE, config.resolveNamespacedURLV3(namespace, "apps"),
				config.getAccessToken());
	}

	/**
	 * Checks if an application exists.
	 *
	 * @param app
	 *            the application to check
	 * @return true if the application exists
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public boolean exists(ApplicationId app) throws IOException, UnauthenticatedException, UnauthorizedException {
		HttpResponse response = restClient.execute(HttpMethod.GET,
				config.resolveNamespacedURLV3(app.getParent(), "apps/" + app.getApplication()), config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);
		return response.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND;
	}

	/**
	 * Waits for an application to be deployed.
	 *
	 * @param app
	 *            the application to check
	 * @param timeout
	 *            time to wait before timing out
	 * @param timeoutUnit
	 *            time unit of timeout
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws TimeoutException
	 *             if the application was not yet deployed before {@code timeout}
	 *             milliseconds
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public void waitForDeployed(final ApplicationId app, long timeout, TimeUnit timeoutUnit)
			throws IOException, UnauthenticatedException, TimeoutException, InterruptedException {

		try {
			Tasks.waitFor(true, new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return exists(app);
				}
			}, timeout, timeoutUnit, 1, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			Throwables.propagateIfPossible(e.getCause(), IOException.class, UnauthenticatedException.class);
		}
	}

	/**
	 * Waits for an application to be deleted.
	 *
	 * @param app
	 *            the application to check
	 * @param timeout
	 *            time to wait before timing out
	 * @param timeoutUnit
	 *            time unit of timeout
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws TimeoutException
	 *             if the application was not yet deleted before {@code timeout}
	 *             milliseconds
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public void waitForDeleted(final ApplicationId app, long timeout, TimeUnit timeoutUnit)
			throws IOException, UnauthenticatedException, TimeoutException, InterruptedException {

		try {
			Tasks.waitFor(false, new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return exists(app);
				}
			}, timeout, timeoutUnit, 1, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			Throwables.propagateIfPossible(e.getCause(), IOException.class, UnauthenticatedException.class);
		}
	}

	/**
	 * Deploys an application.
	 *
	 * @param jarFile
	 *            JAR file of the application to deploy
	 * @throws IOException
	 *             if a network error occurred
	 */
	public void deploy(NamespaceId namespace, File jarFile) throws IOException, UnauthenticatedException {
		Map<String, String> headers = ImmutableMap.of("X-Archive-Name", jarFile.getName());
		deployApp(namespace, jarFile, headers);
	}

	/**
	 * Deploys an application with a serialized application configuration string.
	 *
	 * @param namespace
	 *            namespace to which the application should be deployed
	 * @param jarFile
	 *            JAR file of the application to deploy
	 * @param appConfig
	 *            serialized application configuration
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void deploy(NamespaceId namespace, File jarFile, @Nullable String appConfig)
			throws IOException, UnauthenticatedException {
		deploy(namespace, jarFile, appConfig, null);
	}

	/**
	 * Deploys an application with a serialized application configuration string and
	 * an owner.
	 *
	 * @param namespace
	 *            namespace to which the application should be deployed
	 * @param jarFile
	 *            JAR file of the application to deploy
	 * @param appConfig
	 *            serialized application configuration
	 * @param ownerPrincipal
	 *            the principal of application owner
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void deploy(NamespaceId namespace, File jarFile, @Nullable String appConfig, @Nullable String ownerPrincipal)
			throws IOException, UnauthenticatedException {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Archive-Name", jarFile.getName());
		if (appConfig != null) {
			headers.put("X-App-Config", appConfig);
		}
		if (ownerPrincipal != null) {
			headers.put("X-Principal", ownerPrincipal);
		}
		deployApp(namespace, jarFile, headers);
	}

	/**
	 * Deploys an application with an application configuration object.
	 *
	 * @param namespace
	 *            namespace to which the application should be deployed
	 * @param jarFile
	 *            JAR file of the application to deploy
	 * @param appConfig
	 *            application configuration object
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void deploy(NamespaceId namespace, File jarFile, Config appConfig)
			throws IOException, UnauthenticatedException {
		deploy(namespace, jarFile, GSON.toJson(appConfig));
	}

	/**
	 * Creates an application with a version using an existing artifact.
	 *
	 * @param appId
	 *            the id of the application to add
	 * @param createRequest
	 *            the request body, which contains the artifact to use and any
	 *            application config
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void deploy(ApplicationId appId, AppRequest<?> createRequest) throws IOException, UnauthenticatedException {
		URL url = config.resolveNamespacedURLV3(new NamespaceId(appId.getNamespace()),
				String.format("apps/%s/versions/%s/create", appId.getApplication(), appId.getVersion()));
		HttpRequest request = HttpRequest.post(url).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withBody(GSON.toJson(createRequest)).build();
		restClient.upload(request, config.getAccessToken());
	}

	/**
	 * Update an existing app to use a different artifact version or config.
	 *
	 * @param appId
	 *            the id of the application to update
	 * @param updateRequest
	 *            the request to update the application with
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 * @throws NotFoundException
	 *             if the app or requested artifact could not be found
	 * @throws BadRequestException
	 *             if the request is invalid
	 */
	public void update(ApplicationId appId, AppRequest<?> updateRequest) throws IOException, UnauthenticatedException,
			NotFoundException, BadRequestException, UnauthorizedException {

		URL url = config.resolveNamespacedURLV3(appId.getParent(),
				String.format("apps/%s/update", appId.getApplication()));
		HttpRequest request = HttpRequest.post(url).withBody(GSON.toJson(updateRequest)).build();
		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND,
				HttpURLConnection.HTTP_BAD_REQUEST);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundException("app or app artifact");
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(String.format("Bad Request. Reason: %s", response.getResponseBodyAsString()));
		}
	}

	private void deployApp(NamespaceId namespace, File jarFile, Map<String, String> headers)
			throws IOException, UnauthenticatedException {
		URL url = config.resolveNamespacedURLV3(namespace, "apps");
		HttpRequest request = HttpRequest.post(url).addHeaders(headers)
				.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM).withBody(jarFile).build();
		restClient.upload(request, config.getAccessToken());
	}

	/**
	 * Lists all programs of a type.
	 *
	 * @param programType
	 *            type of the programs to list
	 * @return list of {@link ProgramRecord}s
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ProgramRecord> listAllPrograms(NamespaceId namespace, ProgramType programType)
			throws IOException, UnauthenticatedException, UnauthorizedException {

		Preconditions.checkArgument(programType.isListable());

		String path = programType.getCategoryName();
		URL url = config.resolveNamespacedURLV3(namespace, path);
		HttpRequest request = HttpRequest.get(url).build();
		
		HttpResponse response = restClient.execute(request, config.getAccessToken());
		/*
		 * Extraction of response body changed, as initial 
		 * implementation has an issue with Gson
		 */		
		List<Map<String,Object>> programs = ObjectResponse.fromJsonBody(response, new TypeToken<List<Map<String, Object>>>() {
			private static final long serialVersionUID = -4499509812144319914L;
		}).getResponseObject();
		
		List<ProgramRecord> records = new ArrayList<ProgramRecord>();
		for (Map<String,Object> program: programs) {
			records.add(new ProgramRecord(program));
		}
		
		return records;
	}

	/**
	 * Lists all programs.
	 *
	 * @return list of {@link ProgramRecord}s
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public Map<ProgramType, List<ProgramRecord>> listAllPrograms(NamespaceId namespace)
			throws IOException, UnauthenticatedException, UnauthorizedException {

		ImmutableMap.Builder<ProgramType, List<ProgramRecord>> allPrograms = ImmutableMap.builder();
		for (ProgramType programType : ProgramType.values()) {
			if (programType.isListable()) {
				List<ProgramRecord> programRecords = Lists.newArrayList();
				programRecords.addAll(listAllPrograms(namespace, programType));
				allPrograms.put(programType, programRecords);
			}
		}
		return allPrograms.build();
	}

	/**
	 * Lists programs of a type belonging to an application.
	 *
	 * @param app
	 *            the application
	 * @param programType
	 *            type of the programs to list
	 * @return list of {@link ProgramRecord}s
	 * @throws ApplicationNotFoundException
	 *             if the application with the given ID was not found
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ProgramRecord> listPrograms(ApplicationId app, ProgramType programType)
			throws ApplicationNotFoundException, IOException, UnauthenticatedException, UnauthorizedException {

		Preconditions.checkArgument(programType.isListable());

		List<ProgramRecord> programs = Lists.newArrayList();
		for (ProgramRecord program : listPrograms(app)) {
			if (programType.equals(program.getType())) {
				programs.add(program);
			}
		}
		return programs;
	}

	/**
	 * Lists programs of a type belonging to an application.
	 *
	 * @param app
	 *            the application
	 * @return Map of {@link ProgramType} to list of {@link ProgramRecord}s
	 * @throws ApplicationNotFoundException
	 *             if the application with the given ID was not found
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public Map<ProgramType, List<ProgramRecord>> listProgramsByType(ApplicationId app)
			throws ApplicationNotFoundException, IOException, UnauthenticatedException, UnauthorizedException {

		Map<ProgramType, List<ProgramRecord>> result = Maps.newHashMap();
		for (ProgramType type : ProgramType.values()) {
			result.put(type, Lists.<ProgramRecord>newArrayList());
		}
		for (ProgramRecord program : listPrograms(app)) {
			result.get(program.getType()).add(program);
		}
		return result;
	}

	/**
	 * Lists all programs belonging to an application.
	 *
	 * @param app
	 *            the application
	 * @return List of all {@link ProgramRecord}s
	 * @throws ApplicationNotFoundException
	 *             if the application with the given ID was not found
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<ProgramRecord> listPrograms(ApplicationId app)
			throws ApplicationNotFoundException, IOException, UnauthenticatedException, UnauthorizedException {

		String path = String.format("apps/%s/versions/%s", app.getApplication(), app.getVersion());
		URL url = config.resolveNamespacedURLV3(app.getParent(), path);
		HttpRequest request = HttpRequest.get(url).build();

		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ApplicationNotFoundException(app);
		}

		Map<String, Object> detail = ObjectResponse.fromJsonBody(response, new TypeToken<Map<String, Object>>() {
			private static final long serialVersionUID = 123808082189315467L;
		}).getResponseObject();

		return new ApplicationDetail(detail).getPrograms();
	}

	/**
	 * Add a schedule to an application.
	 *
	 * @param app
	 *            the application
	 * @param scheduleDetail
	 *            the schedule to be added
	 */
	public void addSchedule(ApplicationId app, ScheduleDetail scheduleDetail) throws ApplicationNotFoundException,
			IOException, UnauthenticatedException, UnauthorizedException, BadRequestException {
		String path = String.format("apps/%s/versions/%s/schedules/%s", app.getApplication(), app.getVersion(),
				scheduleDetail.getName());
		HttpResponse response = restClient.execute(HttpMethod.PUT, config.resolveNamespacedURLV3(app.getParent(), path),
				GSON.toJson(scheduleDetail), ImmutableMap.<String, String>of(), config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_BAD_REQUEST);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ApplicationNotFoundException(app);
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(String.format("Bad Request. Reason: %s", response.getResponseBodyAsString()));
		}
	}

	/**
	 * Enable a schedule in an application.
	 *
	 * @param scheduleId
	 *            the id of the schedule to be enabled
	 */
	public void enableSchedule(ScheduleId scheduleId) throws ApplicationNotFoundException, IOException,
			UnauthenticatedException, UnauthorizedException, BadRequestException {
		String path = String.format("apps/%s/versions/%s/program-type/schedules/program-id/%s/action/enable",
				scheduleId.getApplication(), scheduleId.getVersion(), scheduleId.getSchedule());
		HttpResponse response = restClient.execute(HttpMethod.PUT,
				config.resolveNamespacedURLV3(scheduleId.getParent().getParent(), path), config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_BAD_REQUEST);

		int responseCode = response.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new ApplicationNotFoundException(scheduleId.getParent());
		} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(String.format("Bad Request. Reason: %s", response.getResponseBodyAsString()));
		}
	}
}
