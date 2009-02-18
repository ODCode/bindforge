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

package org.bindforge.internal

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

import compiler._


class BindforgeBundle(selfBundle: Bundle, targetBundle: Bundle, scripts: Iterator[URL]) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val settings = new Settings(null)
  private val reporter = new LogReporter(LoggerFactory.getLogger(classOf[ScalaCompiler]), settings)
  private val compiler = new ScalaCompiler(settings, reporter, getAbstractFileClassPath)
  compiler.genJVM.outputDir = new PlainFile(targetBundle.getBundleContext.getDataFile("."))

  def compile() {
    val bundleAf = getAbstractFile(targetBundle)
    val scriptsAf = scripts.map {s =>
      val af = bundleAf.lookupName(s.getFile.substring(1), false)
      new BatchSourceFile(af)
    }.toList

    val run = new compiler.Run

    logger.debug("Compiling {} source file(s)", scriptsAf.length)
    val startTime = System.currentTimeMillis
    run.compileSources(scriptsAf)
    val endTime = System.currentTimeMillis
    logger.debug("Compiling completed. Duration: {} sec", (endTime - startTime) / 1000.0)
    if (reporter.hasErrors) {
      println(reporter.toString)
    }
    reporter.flush
  }

  def loadClass(fqcn: String): Class[_] = {
    val cl = getBundleClassLoader
    val classLoader = new AbstractFileClassLoader(compiler.genJVM.outputDir, cl)
    Class.forName(fqcn, true, classLoader)
  }

  def getAbstractFileClassPath(): Array[AbstractFile] = {
    getBundlesForClassPath.map(getAbstractFile(_))
  }

  def getBundleClassLoader(): ClassLoader = {
    val bundles = getBundlesForClassPath
    var cl = new BundleClassLoader(bundles(0), classOf[BindforgeBundle].getClassLoader)
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

  def getPackageAdmin(): PackageAdmin = {
    val ref = selfBundle.getBundleContext.getServiceReference(classOf[PackageAdmin].getName)
    if (ref == null) throw new IllegalStateException("PackageAdmin is not registered")
    selfBundle.getBundleContext.getService(ref).asInstanceOf[PackageAdmin]
  }
  
}
