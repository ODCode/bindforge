/*
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

package org.bindforge.common.integrationtest

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

  addBundle("org.bindforge", "bindforge.scala-library", "2.7.3")
  addBundle("org.bindforge", "bindforge.common.integrationtest", "1.0.0")
  addBundle("org.ops4j.pax.logging", "pax-logging-api", "1.3.0")
  addBundle("org.ops4j.pax.logging", "pax-logging-service", "1.3.0")
  addWrappedBundle("org.scalatest", "scalatest", "0.9.4")

  def addWrappedBundle(groupId: String, artifactId: String, version: String) {
    bundles += wrappedBundle(mavenBundle().groupId(groupId).artifactId(artifactId).version(version).getURL()).getURL()
  }

  def addBundle(groupId: String, artifactId: String, version: String) {
    bundles += mavenBundle().groupId(groupId).artifactId(artifactId).version(version).getURL()
  }

  def baseConfiguration(): List[Option] = {
    List(
      frameworks(CoreOptions.equinox()),
        provision(bundles.toArray: _*)
      );
  }

  def runScalaTest() {
    runScalaTest(this)
  }

  def runScalaTest(obj: Suite) {
    println("==================")
    println("Starting ScalaTest")
    println("==================")
    val suite = new SuperSuite(List(obj))
    suite.execute(None, new IntegrationReporter, new Stopper {}, Set(), Set(), Map(), None)
    println("==================")
    println("End ScalaTest")
    println("==================")
  }

}
