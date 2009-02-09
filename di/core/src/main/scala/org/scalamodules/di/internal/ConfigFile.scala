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

package org.scalamodules.di.internal

import java.net.URL
import java.net.URISyntaxException
import java.io.File
import java.util.Scanner
import scala.tools.nsc.util.{SourceFile, BatchSourceFile}
import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.PlainFile
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.reporters.ConsoleReporter
import org.osgi.framework.{Bundle, BundleContext}
import org.slf4j.LoggerFactory

import org.scalamodules.di.internal.compiler._


class ConfigFile(context: BundleContext, url: URL) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val scanner  = new Scanner(url.openStream)
  private val builder = new StringBuilder

  private val packageName = calculatePackageName()
  private val className = "BundleBindingConfig"
  private val FQCN = packageName + "." + className

  builder.append("package " + packageName + " \n")
  builder.append("class " + className + " extends org.scalamodules.di.BindingConfig { \n");
  while (scanner.hasNextLine) {
    builder.append(scanner.nextLine + "\n")
  }
  builder.append("\n}\n");

  println(builder.toString)

  def compile() {
    val bundles = context.getBundles
    val cp = getClassPath(bundles)
    val settings = new Settings(null)
    val reporter = new LogReporter(LoggerFactory.getLogger(classOf[ScalaCompiler]), settings)
    val compiler = new ScalaCompiler(settings, reporter, cp)

    val run = new compiler.Run

    run.compileSources(List(new BatchSourceFile(FQCN, builder.toString.toCharArray)))
    if (reporter.hasErrors) {
      println(reporter.toString)
    }
    reporter.flush

    val parentCl = classOf[ConfigFile].getClassLoader
    val classLoader = new AbstractFileClassLoader(compiler.genJVM.outputDir, parentCl)
    val script = Class.forName(FQCN, true, classLoader)
    script.newInstance

    1
  }

  def getClassPath(bundles: Array[Bundle]): Array[AbstractFile] = {

    val bundleFs = new Array[AbstractFile](bundles.length)

    for ((b, i) <- bundles.zipWithIndex) {
      val url = bundles(i).getResource("/")
      if (url != null) {
        if ("file".equals(url.getProtocol())) {
          try {
            bundleFs(i) = new PlainFile(new File(url.toURI()))
          }
          catch {
            case e: URISyntaxException => throw new IllegalArgumentException("Can't determine url of bundle " + i)
          }
        }
        else {
          bundleFs(i) = BundleFS.create(bundles(i))
        }
      }
    }
    bundleFs
  }

  def calculatePackageName(): String = {
    "org.taileron._generated_" +
    context.getBundle.getSymbolicName +
    context.getBundle.getBundleId
  }
  
}
