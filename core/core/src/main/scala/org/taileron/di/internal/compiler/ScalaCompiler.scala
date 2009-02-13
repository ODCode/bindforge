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

package org.taileron.di.internal.compiler

import scala.tools.nsc.{Settings, Global}
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.reporters.Reporter


/**
 * Extendend Scala compiler which supports a class path with {@link AbstractFile} entries.
 * Note: this implementation does not support MSIL (.NET).
 */
class ScalaCompiler(settings: Settings, reporter: Reporter, classes: Array[AbstractFile])
extends Global(settings, reporter) {

  override lazy val classPath0 = new ScalaClasspath(false && onlyPresentation)

  override lazy val classPath = {
    require(!forMSIL, "MSIL not supported")
    new classPath0.BuildClasspath(settings.classpath.value, settings.sourcepath.value,
                                  settings.outdir.value, settings.bootclasspath.value, settings.extdirs.value,
                                  settings.Xcodebase.value, classes)
  }

}

