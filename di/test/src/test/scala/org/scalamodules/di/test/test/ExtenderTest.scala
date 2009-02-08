
package org.scalamodules.di.test.test

import org.slf4j.LoggerFactory

import org.junit.Test
import org.junit.Assert._
import org.junit.runner.RunWith

import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit.JUnit4TestRunner
import org.ops4j.pax.exam.junit.Configuration

import com.google.inject.{Injector, Guice, Key}
import org.ops4j.peaberry.Peaberry

import org.osgi.framework.BundleContext
import org.osgi.service.packageadmin.PackageAdmin

import org.scalamodules.di._
import org.scalamodules.common.integrationtest._


@RunWith(classOf[JUnit4TestRunner])
class ExtenderTest extends OSGiTest {

  //private val logger = LoggerFactory.getLogger(this.getClass)

  addBundle("org.scalamodules", "scalamodules.di", "1.0.0")
  addBundle("org.scalamodules", "scalamodules.di.testbundle", "1.0.0")

  addBundle("org.ops4j", "peaberry", "1.0")
  addWrappedBundle("org.ops4j.peaberry.dependencies", "guice", "2.0-SNAPSHOT")
  addWrappedBundle("org.ops4j.peaberry.dependencies", "aopalliance", "1.0-SNAPSHOT")

  @Inject
  var context: BundleContext = _

  @Configuration
  def config() = baseConfiguration

  @Test
  def wrapper() = runScalaTest

  def testServiceImport() {
    println("starting thread to buy some time")
    println("will be fixed soon!")
    try {
      Thread.sleep(3000)
      println("done")
    }
    catch {
      case _ =>
    }
  }

}

