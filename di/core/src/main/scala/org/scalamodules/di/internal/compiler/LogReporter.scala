/*
 * Copyright 2009 Roman Roelofsen
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

package org.scalamodules.di.internal.compiler

import org.slf4j.Logger

import scala.tools.nsc.Settings
import scala.tools.nsc.util.Position


class LogReporter(logger: Logger, settings: Settings) extends BacklogReporter(settings) {

  override def display(pos: Position, msg: String, severity: Severity) {
    super.display(pos, msg, severity)
    if (INFO.equals(severity)) {
      logger.info("{}: {}", msg, pos)
    }
    else if (WARNING.equals(severity)) {
      logger.warn("{}: {}", msg, pos)
    }
    else if (ERROR.equals(severity)) {
      logger.error("{}: {}", msg, pos)
    }
    else {
      throw new IllegalArgumentException("Severtiy out of range")
    }
  }


}
