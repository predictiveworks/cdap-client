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

import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.util.RESTClient;
/*
 * Changed to project internal exception to reduce dependencies
 * Dr. Krusche & Partner PartG
 */
import co.cask.cdap.client.common.BadRequestException;
import co.cask.cdap.client.common.NotFoundException;
import co.cask.cdap.client.common.ServiceNotEnabledException;
import co.cask.cdap.client.common.UnauthenticatedException;
import co.cask.cdap.client.proto.Instances;
import co.cask.cdap.client.proto.SystemServiceLiveInfo;
import co.cask.cdap.client.proto.SystemServiceMeta;
import co.cask.cdap.proto.id.SystemServiceId;
/*
 * Changed to project internal exception to reduce dependencies
 * Dr. Krusche & Partner PartG
 */
import co.cask.cdap.client.exception.UnauthorizedException;
import co.cask.common.http.HttpMethod;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpResponse;
import co.cask.common.http.ObjectResponse;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Provides ways to monitor CDAP.
 */
@Beta
public class MonitorClient {

	private static final Gson GSON = new Gson();

	private static final TypeToken<Map<String, Object>> RECORD_TYPE = new TypeToken<Map<String, Object>>() {
		private static final long serialVersionUID = 2219781943863482100L;
	};

	private static final TypeToken<List<Map<String, Object>>> RECORDLIST_TYPE = new TypeToken<List<Map<String, Object>>>() {
		private static final long serialVersionUID = 5617417745251214129L;
	};

	private final RESTClient restClient;
	private final ClientConfig config;

	@Inject
	public MonitorClient(ClientConfig config, RESTClient restClient) {
		this.config = config;
		this.restClient = restClient;
	}

	public MonitorClient(ClientConfig config) {
		this(config, new RESTClient(config));
	}

	/**
	 * Gets the live info of a system service.
	 *
	 * @param serviceName
	 *            Name of the system service
	 * @return live info of the system service
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public SystemServiceLiveInfo getSystemServiceLiveInfo(String serviceName)
			throws IOException, UnauthenticatedException, NotFoundException, UnauthorizedException {

		URL url = config.resolveURLV3(String.format("system/services/%s/live-info", serviceName));
		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND);

		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundException(new SystemServiceId(serviceName));
		}
		/*
		 * Extraction of response body changed, as initial implementation has an issue
		 * with Gson
		 */
		Map<String, Object> record = ObjectResponse.fromJsonBody(response, RECORD_TYPE).getResponseObject();
		return new SystemServiceLiveInfo(record);

	}

	/**
	 * Lists all system services.
	 *
	 * @return list of {@link SystemServiceMeta}s.
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public List<SystemServiceMeta> listSystemServices()
			throws IOException, UnauthenticatedException, UnauthorizedException {
		URL url = config.resolveURL("system/services");
		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
		/*
		 * Extraction of response body changed, as initial implementation has an issue
		 * with Gson
		 */
		List<Map<String, Object>> records = ObjectResponse.fromJsonBody(response, RECORDLIST_TYPE).getResponseObject();

		List<SystemServiceMeta> result = new ArrayList<SystemServiceMeta>();
		for (Map<String, Object> record : records) {
			result.add(new SystemServiceMeta(record));
		}
		return result;
	}

	/**
	 * Gets the status of a system service.
	 *
	 * @param serviceName
	 *            Name of the system service
	 * @return status of the system service
	 * @throws IOException
	 *             if a network error occurred
	 * @throws NotFoundException
	 *             if the system service with the specified name could not be found
	 * @throws BadRequestException
	 *             if the operation was not valid for the system service
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public String getSystemServiceStatus(String serviceName) throws IOException, NotFoundException, BadRequestException,
			UnauthenticatedException, UnauthorizedException {

		URL url = config.resolveURL(String.format("system/services/%s/status", serviceName));
		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_BAD_REQUEST);
		String responseBody = new String(response.getResponseBody());
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundException(new SystemServiceId(serviceName));
		} else if (response.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(responseBody);
		}
		Map<String, String> status = GSON.fromJson(responseBody, new TypeToken<Map<String, String>>() {
			private static final long serialVersionUID = -953674440060174792L;
		}.getType());
		return status.get("status");
	}

	/**
	 * Gets the status of all system services.
	 *
	 * @return map from service name to service status
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public Map<String, String> getAllSystemServiceStatus()
			throws IOException, UnauthenticatedException, UnauthorizedException {
		URL url = config.resolveURL("system/services/status");
		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
		return ObjectResponse.fromJsonBody(response, new TypeToken<Map<String, String>>() {
			private static final long serialVersionUID = 9202225318061827468L;
		}).getResponseObject();
	}

	/**
	 * @return true if all system services' status is 'OK'; false otherwise
	 * @throws IOException
	 *             if a network error occurred
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public boolean allSystemServicesOk() throws IOException, UnauthenticatedException, UnauthorizedException {
		for (String status : getAllSystemServiceStatus().values()) {
			if (!"OK".equals(status)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sets the number of instances the system service is running on.
	 *
	 * @param serviceName
	 *            name of the system service
	 * @param instances
	 *            number of instances the system service is running on
	 * @throws IOException
	 *             if a network error occurred
	 * @throws NotFoundException
	 *             if the system service with the specified name was not found
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public void setSystemServiceInstances(String serviceName, int instances) throws IOException, NotFoundException,
			BadRequestException, UnauthenticatedException, UnauthorizedException {

		URL url = config.resolveURL(String.format("system/services/%s/instances", serviceName));
		HttpRequest request = HttpRequest.put(url).withBody(GSON.toJson(new Instances(instances))).build();
		HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND,
				HttpURLConnection.HTTP_BAD_REQUEST);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundException(new SystemServiceId(serviceName));
		} else if (response.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new BadRequestException(new String(response.getResponseBody()));
		}
	}

	/**
	 * Gets the number of instances the system service is running on.
	 *
	 * @param serviceName
	 *            name of the system service
	 * @return number of instances the system service is running on
	 * @throws IOException
	 *             if a network error occurred
	 * @throws NotFoundException
	 *             if the system service with the specified name was not found
	 * @throws ServiceNotEnabledException
	 *             if the system service is not currently enabled
	 * @throws UnauthenticatedException
	 *             if the request is not authorized successfully in the gateway
	 *             server
	 */
	public int getSystemServiceInstances(String serviceName) throws IOException, NotFoundException,
			ServiceNotEnabledException, UnauthenticatedException, UnauthorizedException {

		URL url = config.resolveURL(String.format("system/services/%s/instances", serviceName));
		HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken(),
				HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_FORBIDDEN);
		if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundException(new SystemServiceId(serviceName));
		} else if (response.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
			throw new ServiceNotEnabledException(serviceName);
		}

		/*
		 * Extraction of response body changed, as initial implementation has an issue
		 * with Gson
		 */
		Map<String, Object> record = ObjectResponse.fromJsonBody(response, RECORD_TYPE).getResponseObject();
		return new Instances(record).getInstances();

	}

}
