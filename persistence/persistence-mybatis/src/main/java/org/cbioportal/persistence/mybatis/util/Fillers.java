/*
 * Copyright (C) 2017-2021 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.cbioportal.persistence.mybatis.util;

/**
 * Hold some strings used when printing results.
 */
enum Fillers {
  HEADER("------------------"),
  FOOTER("==================");

  private final String string;

  Fillers(String string) {
    this.string = string;
  }

  /**
   * Get the representation of this instace, formatted as String.
   *
   * @return the {@code String} representation of this instance, formatted.
   */
  public final String toFormattedString() {
    return string;
  }
}
