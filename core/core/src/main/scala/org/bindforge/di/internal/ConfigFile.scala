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

package org.bindforge.di.internal

import java.net.URL
import java.net.URISyntaxException
import java.io.File
import java.util.Scanner

import scala.collection.mutable.{ListBuffer, LinkedHashSet}
import scala.tools.nsc.util.{SourceFile, BatchSourceFile}
import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.PlainFile
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.reporters.ConsoleReporter

import org.osgi.framework.{Bundle, BundleContext}
import org.osgi.service.packageadmin.PackageAdmin

import org.slf4j.LoggerFactory
import org.ops4j.pax.swissbox.core.BundleClassLoader

import org.bindforge.di.internal.compiler._


class ConfigFile(selfBundle: Bundle, targetBundle: Bundle, url: URL) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val scanner = new Scanner(url.openStream)
  private val builder = new StringBuilder

  //private val packageName = calculatePackageName()
  //private val className = "BindingConfig"
  private val packageName = "org.bindforge.di.testbundle"
  private val className = "Module"

  private val FQCN = packageName + "." + className

  //builder.append("package " + packageName + " \n")
  //builder.append("class " + className + " extends org.bindforge.di.BindingConfig { \n");
  while (scanner.hasNextLine) {
    builder.append(scanner.nextLine + "\n")
  }
  //builder.append("\n}\n");

  def getBindingConfigClass() {
    // TODO: check if class is already compiled
    val cp = getAbstractFileClassPath
    val settings = new Settings(null)
    val reporter = new LogReporter(LoggerFactory.getLogger(classOf[ScalaCompiler]), settings)
    val compiler = new ScalaCompiler(settings, reporter, cp)
    compiler.genJVM.outputDir = new PlainFile(targetBundle.getBundleContext.getDataFile("."))
    val run = new compiler.Run

    run.compileSources(List(new BatchSourceFile(FQCN, builder.toString.toCharArray)))
    if (reporter.hasErrors) {
      println(reporter.toString)
    }
    reporter.flush

    val cl = getBundleClassLoader
    val classLoader = new AbstractFileClassLoader(compiler.genJVM.outputDir, cl)
    val script = Class.forName(FQCN, true, classLoader)
    script.newInstance

    1
  }

  def getAbstractFileClassPath(): Array[AbstractFile] = {
    getBundlesForClassPath.map(getAbstractFile(_))
  }

  def getBundleClassLoader(): ClassLoader = {
    val bundles = getBundlesForClassPath
    var cl = new BundleClassLoader(bundles(0), classOf[ConfigFile].getClassLoader)
    for (i <- 1 until bundles.length) {
      cl = new BundleClassLoader(bundles(i), cl)
    }
    cl
  }

  def getBundlesForClassPath(): Array[Bundle] = {
    val bundlesFs = new ListBuffer[Bundle]

    // Add target bundle
    if (isReadable(targetBundle)) bundlesFs += targetBundle
    else throw new IllegalStateException("Can not access bundle [" + targetBundle.getSymbolicName + "]")
    
    // Add DI bundle
    if (isReadable(selfBundle)) bundlesFs += selfBundle
    else throw new IllegalStateException("Can not access bundle [" + targetBundle.getSymbolicName + "]")

    // Find and add scala lib and compiler bundle
    getScalaBundles.foreach(bundlesFs += _)

    bundlesFs.toArray
  }

  def isReadable(bundle: Bundle): Boolean = {
    bundle.getResource("/") != null
  }

  def getScalaBundles(): Array[Bundle] = {
    val scalaBundles = new LinkedHashSet[Bundle]

    // Scala library
    val b1 = getPackageAdmin.getExportedPackage("scala").getExportingBundle
    if (b1 != null) scalaBundles += b1

    // Scala compiler
    val b2 = getPackageAdmin.getExportedPackage("scala.tools.nsc").getExportingBundle
    if (b2 != null) scalaBundles += b2

    scalaBundles.toArray
  }

  def getAbstractFile(bundle: Bundle): AbstractFile = {
    /*
     val url = bundle.getResource("/")
     if ("file".equals(url.getProtocol())) {
     try {
     return new PlainFile(new File(url.toURI()))
     }
     catch {
     case e: URISyntaxException =>
     throw new IllegalArgumentException("Can't determine url of bundle " + bundle.getSymbolicName)
     }
     }
     else {
     return BundleFS.create(bundle)
     }
     */
    return BundleFS.create(bundle)
  }

  def calculatePackageName(): String = {
    (targetBundle.getBundleContext.getBundle.getSymbolicName +
     "_" +
     targetBundle.getBundleContext.getBundle.getBundleId).replaceAll("\\.", "_")
  }

  def getPackageAdmin(): PackageAdmin = {
    val ref = selfBundle.getBundleContext.getServiceReference(classOf[PackageAdmin].getName)
    if (ref == null) throw new IllegalStateException("PackageAdmin is not registered")
    selfBundle.getBundleContext.getService(ref).asInstanceOf[PackageAdmin]
  }
  
}
