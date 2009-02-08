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

  private val scanner  = new Scanner(url.openStream)
  private val builder = new StringBuilder


  builder.append("import org.scalamodules.di._ \n");
  builder.append("class Module extends BindingConfig { \n");

  while (scanner.hasNextLine) {
    builder.append(scanner.nextLine + "\n")
  }

  builder.append("\n}\n");

  def compile() {
    val bundles = context.getBundles
    val cp = getClassPath(bundles)
    val settings = new Settings(null)
    val reporter = new LogReporter(LoggerFactory.getLogger(classOf[ScalaCompiler]), settings)
    val compiler = new ScalaCompiler(settings, reporter, cp)

    val run = new compiler.Run
    val code = "class ABC {println(1+1)}A"

    println("start compile")
    run.compileSources(List(new BatchSourceFile("123delme", code.toCharArray)))
    println("end compile")
    if (reporter.hasErrors) {
      println("errors start")
      println(reporter.toString)
      println("errors stop")
    }
    else {
      println("no errors")
    }
    reporter.flush
    println("flushed")

    val parentCl = classOf[ConfigFile].getClassLoader
    val classLoader = new AbstractFileClassLoader(compiler.genJVM.outputDir, parentCl)
    val script = Class.forName("ABC", true, classLoader)
    println(script)
    script.newInstance

    1
  }

  def getClassPath(bundles: Array[Bundle]): Array[AbstractFile] = {

    val bundleFs = new Array[AbstractFile](bundles.length)

    for ((b, i) <- bundles.zipWithIndex) {
      println("analysing bundle " + b)
      val url = bundles(i).getResource("/")
      if (url != null) {
        if ("file".equals(url.getProtocol())) {
          try {
            println("- creating plainfile for " + url)
            bundleFs(i) = new PlainFile(new File(url.toURI()))
          }
          catch {
            case e: URISyntaxException => throw new IllegalArgumentException("Can't determine url of bundle " + i)
          }
        }
        else {
          println("- creating bundlefs for " + bundles(i))
          bundleFs(i) = BundleFS.create(bundles(i))
        }
      }
    }
    bundleFs
  }
  
}
