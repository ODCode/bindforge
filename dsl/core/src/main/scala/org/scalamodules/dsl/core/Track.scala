/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/

package org.scalamodules.dsl.core

import scala.collection.Map
import org.osgi.framework.{BundleContext, ServiceReference}
import org.osgi.util.tracker.ServiceTracker
import org.scalamodules.dsl.core.RichBundleContext.fromBundleContext

/**
 * Helper for tracking services.
 */
class Track[T](context: BundleContext, serviceInterface: Class[T],
               filter: String, a: Option[T => Unit], r: Option[T => Unit]) {

  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")
  require(serviceInterface != null, "Option for add-function must not be null!")
  require(serviceInterface != null, "Option for remove-function must not be null!")
  
  private var tracker: ServiceTracker = _
  
  def this(context: BundleContext, serviceInterface: Class[T]) {
    this(context, serviceInterface, null, None, None)
  }

  // ===========================================================================
  //  API
  // ===========================================================================

  /**
   * Creates a Track with the given filter.
   */
  def withFilter(filter: String): Track[T] =
    new Track(context, serviceInterface, filter, a, r)

  /**
   * Applies the given function to a service being added.
   */
  def onAdd(f: T => Unit): Track[T] = {
    require(f != null, "Add-function must not be null!")
    new Track(context, serviceInterface, filter, Some(f), r)
  }

  /**
   * Applies the given function to a service being removed.
   */
  def onRemove(f: T => Unit): Track[T] = {
    require(f != null, "Remove-function must not be null!")
    new Track(context, serviceInterface, filter, a, Some(f))
  }

  /**
   * Starts tracking.
   */
  def start: Track[T] = {
    println("++start")
    tracker = new ServiceTracker(context, serviceInterface.getName, null) {
      override def addingService(ref: ServiceReference) = {
        println("++addingService")
        val t = context.getService(ref).asInstanceOf[T]  // Cannot be null (-> spec.)
        a match {
          case None    =>
          case Some(f) => f(t)
        }
      }
      override def removedService(ref: ServiceReference, t: AnyRef) = {
        println("++removedService")
        r match {
          case None    =>
          case Some(f) => f(t.asInstanceOf[T])
        }
        context.ungetService(ref)
      }
    }
    tracker.open()
    this
  }

  /**
   * Stops tracking.
   */
  def stop() { tracker.close() }
}
