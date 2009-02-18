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

package org.bindforge.di.test.test

import java.util.Hashtable

import org.junit.Test
import org.junit.Assert._
import org.junit.runner.RunWith

import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit.JUnit4TestRunner
import org.ops4j.pax.exam.junit.Configuration

import com.google.inject.{Injector, Guice, Key}
import org.ops4j.peaberry.Peaberry

import org.osgi.framework.{Bundle, BundleContext}
import org.osgi.service.packageadmin.PackageAdmin

import org.bindforge._
import org.bindforge.common.integrationtest._
import org.bindforge.test.testbundle._


@RunWith(classOf[JUnit4TestRunner])
class BundleTest extends OSGiTest {

  addBundle("org.bindforge", "bindforge", "1.0.0")
  addBundle("org.bindforge", "bindforge.testbundle", "1.0.0")

  @Inject
  var context: BundleContext = _

  @Configuration
  def config() = baseConfiguration

  @Test
  def wrapper() = {
    waitForTestBundle
    runScalaTest
  }

  /**
   */
  def waitForTestBundle() {
    val utils = new OSGiTestUtils(context)
    val bfs = utils.getService[BindforgeService]
    val bid = utils.getBundleIdBySymbolicName("org.bindforge.testbundle")
    while (!bfs.isBundleTracked(bid)) {
      Thread.sleep(50)
    }
  }

  /**
   */
  def testServiceImport() {
    class SimpleModule extends Config {
      bind [PackageAdmin] importService
    }
    val i = Guice.createInjector(new SimpleModule().create(), Peaberry.osgiModule(context))

    val pa = i.getInstance(Key.get(classOf[PackageAdmin]))
    assert(pa.isInstanceOf[PackageAdmin])
  }

  /**
   */
  def testServiceImportWithFilter() {
    val p1 = new Hashtable[String, Object]
    p1.put("testkey", "1")
    context.registerService(classOf[IdService].getName, new IdServiceImpl("1"), p1)

    val p2 = new Hashtable[String, Object]
    p2.put("testkey", "2")
    context.registerService(classOf[IdService].getName, new IdServiceImpl("2"), p2)

    class SimpleModule extends Config {
      bind [IdService] importService "(testkey=2)"
    }
    val i = Guice.createInjector(new SimpleModule().create(), Peaberry.osgiModule(context))

    val ids = i.getInstance(Key.get(classOf[IdService]))
    assert(ids.isInstanceOf[IdService])
    assert(ids.id == "2")
  }

  /**
   */
  def testServiceExport() {
    val util = new OSGiTestUtils(context)
    val (service, props) = util.getServiceWithProperties[PersonService]
    assert(service.isInstanceOf[PersonService])
    assert(props("key1") == "value1")
    assert(props("key2") == "value2")
  }

}

