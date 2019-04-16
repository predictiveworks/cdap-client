/*
 * Copyright © 2017 Cask Data, Inc.
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

package co.cask.cdap.client.common;

import co.cask.cdap.api.common.HttpErrorStatusProvider;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Similar to Exception, but responds with a custom error code.
 */
public class ServiceException extends Exception implements HttpErrorStatusProvider {
	private static final long serialVersionUID = -2240178100123855294L;
	private final HttpResponseStatus httpResponseStatus;

	public ServiceException(Throwable cause, HttpResponseStatus httpResponseStatus) {
		super(cause);
		this.httpResponseStatus = httpResponseStatus;
	}

	@Override
	public int getStatusCode() {
		return httpResponseStatus.code();
	}
}
