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

import org.junit.Test
import org.junit.runner.RunWith

import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.junit.JUnit4TestRunner
import org.ops4j.pax.exam.junit.Configuration
import org.ops4j.pax.exam.CoreOptions.options

import org.osgi.framework.BundleContext

import org.bindforge.common.integrationtest._


@RunWith(classOf[JUnit4TestRunner])
class BundleTest extends OSGiTest {

  addBundle("org.bindforge", "bindforge", "0.5.0")
  addBundle("org.bindforge", "bindforge.testbundle", "1.0.0")
  addBundle("org.bindforge", "bindforge.common.util", "1.0.0")

  @Inject
  var context: BundleContext = _

  @Configuration
  def config() = options((Bridge.configProfile :: this.baseConfiguration).toArray: _*)

  @Test
  def wrapper() = {
    waitForTestBundle
    runScalaTest(new BundleTestWrapped(context))
  }

  def waitForTestBundle() {
    val utils = new OSGiTestUtils(context)
    val bfs = utils.getService[BindforgeService]
    val bid = utils.getBundleIdBySymbolicName("org.bindforge.testbundle")
    while (!bfs.isBundleTracked(bid)) {
      Thread.sleep(50)
    }
  }

}

