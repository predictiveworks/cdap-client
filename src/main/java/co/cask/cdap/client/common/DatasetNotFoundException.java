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

package co.cask.cdap.client.common;

import co.cask.cdap.proto.id.DatasetId;

/**
 * Thrown when a dataset was not found.
 */
public class DatasetNotFoundException extends NotFoundException {

  private final DatasetId dataset;

  public DatasetNotFoundException(DatasetId dataset) {
    super(dataset);
    this.dataset = dataset;
  }

  public DatasetId getId() {
    return dataset;
  }
}
