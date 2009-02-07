
package org.scalamodules.common.integrationtest

import scala.collection.mutable.ListBuffer

import org.ops4j.pax.exam.Option
import org.ops4j.pax.exam.CoreOptions
import org.ops4j.pax.exam.CoreOptions.frameworks
import org.ops4j.pax.exam.CoreOptions.mavenBundle
import org.ops4j.pax.exam.CoreOptions.options
import org.ops4j.pax.exam.CoreOptions.provision
import org.ops4j.pax.exam.CoreOptions.wrappedBundle

import org.scalatest.{SuperSuite, Suite, Stopper}

import org.osgi.framework.BundleContext


class OSGiTest extends Suite {

  private val bundles = new ListBuffer[String]

  addWrappedBundle("org.scala-lang", "scala-library", "2.7.3")
  addBundle("org.scalamodules", "scalamodules.common.integrationtest", "1.0.0")
  addBundle("org.ops4j.pax.logging", "pax-logging-api", "1.3.0")
  addBundle("org.ops4j.pax.logging", "pax-logging-service", "1.3.0")
  addWrappedBundle("org.scalatest", "scalatest", "0.9.4")

  def addWrappedBundle(groupId: String, artifactId: String, version: String) {
    bundles += wrappedBundle(mavenBundle().groupId(groupId).artifactId(artifactId).version(version).getURL()).getURL()
  }

  def addBundle(groupId: String, artifactId: String, version: String) {
    bundles += mavenBundle().groupId(groupId).artifactId(artifactId).version(version).getURL()
  }

  def baseConfiguration(): Array[Option] = {
    return options(
      frameworks(CoreOptions.felix()),
        provision(bundles.toArray: _*));
  }

  def runScalaTest {
    println("==================")
    println("Starting ScalaTest")
    println("==================")
    val suite = new SuperSuite(List(this))
    suite.execute(None, new IntegrationReporter, new Stopper {}, Set(), Set(), Map(), None)
    println("==================")
    println("End ScalaTest")
    println("==================")
  }

}
