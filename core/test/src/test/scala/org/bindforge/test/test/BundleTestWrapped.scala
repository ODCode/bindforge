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

package org.bindforge.test.test

import java.util.Hashtable

import com.google.inject._
import org.ops4j.peaberry.Peaberry
import org.scalatest.Suite

import org.osgi.framework._
import org.osgi.service.packageadmin.PackageAdmin
import org.osgi.service.cm.{Configuration, ConfigurationAdmin}

import org.bindforge.common.util.jcl.Conversions._
import org.bindforge.common.integrationtest.OSGiTestUtils
import org.bindforge.test.testbundle._


class BundleTestWrapped(context: BundleContext) extends Suite {

  def testServiceImport() {
    class SimpleModule extends Config {
      bind [PackageAdmin] importService
    }
    val i = Guice.createInjector(new SimpleModule().create(), Peaberry.osgiModule(context))

    val pa = i.getInstance(Key.get(classOf[PackageAdmin]))
    assert(pa.isInstanceOf[PackageAdmin])
  }

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

  def testServiceExport() {
    val util = new OSGiTestUtils(context)
    val (service, props) = util.getServiceWithProperties[ExportServiceWithProps]
    assert(service.isInstanceOf[ExportServiceWithProps])
    assert(service.isInstanceOf[ExportServiceWithPropsImpl])
    assert(props("key1") == "value1")
    assert(props("key2") == "value2")
  }
  
  def testServiceExportOnlyOne() {
    assert(context.getServiceReferences(classOf[ExportServiceWithProps].getName, null).length == 1)
  }

  def testServiceExportHandleServiceRegistration() {
    val service = new OSGiTestUtils(context).getService[ExportServiceWithPropsClient]
    val handle = service.exportServiceWithPropsHandle
    assert(handle != null)
    assert(handle.isInstanceOf[ServiceRegistration])
    assert(handle.getReference.getProperty("key1") == "value1")
  }

  def testServiceExportSelfHandle() {
    val service = new OSGiTestUtils(context).getService[ExportServiceWithPropsClient]
    val handle = service.selfExportHandle
    assert(handle != null)
    assert(handle.isInstanceOf[ServiceRegistration])
  }

  def testConfigAdmin() {
    val util = new OSGiTestUtils(context)
    val service = util.getService[ServiceWithConfig]
    val configAdmin = util.getService[ConfigurationAdmin]
    val conf = configAdmin.getConfiguration("servicewithconfig.pid", null)

    var configureFired = false
    while (service.lastConfig == null) {
      if (!configureFired) {
        conf.update(Map("username" -> "Joe", "password" -> "secret", "complex.var.name" -> "value"))
        configureFired = true
      }
      Thread.sleep(50)
    }
    assert(service.username === "Joe")
    assert(service.getPassword === "secret")
    assert(service.lastConfig.get("username") === service.username)
    assert(service.lastConfig.get("password") === service.getPassword)
  }

}

