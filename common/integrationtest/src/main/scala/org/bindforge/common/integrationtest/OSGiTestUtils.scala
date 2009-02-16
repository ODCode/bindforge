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

package org.bindforge.common.integrationtest

import scala.reflect.Manifest
import scala.collection.mutable
import org.osgi.framework.BundleContext


class OSGiTestUtils(context: BundleContext) {

  def getService[A <: Object](implicit clazz: Manifest[A]): A = {
    val ref = context.getServiceReference(clazz.erasure.getName)
    context.getService(ref).asInstanceOf[A]
  }

  def getServiceWithProperties[A <: Object](implicit clazz: Manifest[A]): (A, scala.collection.Map[String, Object]) = {
    val ref = context.getServiceReference(clazz.erasure.getName)
    val dict = new mutable.HashMap[String, Object]()
    ref.getPropertyKeys.foreach(k => dict.put(k, ref.getProperty(k)))
    (context.getService(ref).asInstanceOf[A], dict)
  }

  def getBundleIdBySymbolicName(name: String): Long = {
    val matches = context.getBundles().filter(_.getSymbolicName == name)
    matches(0).getBundleId
  }

}
