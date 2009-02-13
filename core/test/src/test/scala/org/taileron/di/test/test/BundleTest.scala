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

package org.taileron.di.test.test

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

import org.taileron.di._
import org.taileron.common.integrationtest._


class AService {
}

@RunWith(classOf[JUnit4TestRunner])
class BundleTest extends OSGiTest {

  addBundle("org.taileron", "taileron.di", "1.0.0")
  addBundle("org.taileron", "taileron.di.testbundle", "1.0.0")

  addBundle("org.ops4j", "peaberry", "1.0")
  addWrappedBundle("org.ops4j.peaberry.dependencies", "guice", "2.0-SNAPSHOT")
  addWrappedBundle("org.ops4j.peaberry.dependencies", "aopalliance", "1.0-SNAPSHOT")

  @Inject
  var context: BundleContext = _

  @Configuration
  def config() = baseConfiguration

  @Test
  def wrapper() = runScalaTest


  /**
   */
  def testServiceImport() {
    class SimpleModule extends BindingConfig {
      importService [PackageAdmin]
    }
    val i = Guice.createInjector(new SimpleModule().create(), Peaberry.osgiModule(context))

    val pa = i.getInstance(Key.get(classOf[PackageAdmin]))
    assert(pa != null)
    assert(pa.isInstanceOf[PackageAdmin])
  }

  /**
   */
  def testServiceExport() {
    class SimpleModule extends BindingConfig {
      bind [AService] spec {
        exportService("key1" -> "value1", "key2" -> "value2")
      }
    }
    val i = Guice.createInjector(new SimpleModule().create(), Peaberry.osgiModule(context))
    context.getAllServiceReferences(null, null).foreach{ref =>
      ref.getPropertyKeys.foreach{key =>
      }
    }
  }

  /**
   */
  def testXXX() {
    println("starting thread to buy some time")
    println("will be fixed soon!")
    try {
      Thread.sleep(3000)
      println("done sleeping")
    }
    catch {
      case _ =>
    }
  }

}

