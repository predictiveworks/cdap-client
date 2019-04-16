/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.client.explore.service;

/**
 * Exception thrown by {@link Explore}.
 */
public class ExploreException extends Exception {
	private static final long serialVersionUID = -5398630544090549168L;

	public ExploreException(String s) {
		super(s);
	}

	public ExploreException(Throwable throwable) {
		super(throwable);
	}

	public ExploreException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
