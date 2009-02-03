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
import org.scalamodules.dsl.core.RichBundleContext.fromBundleContext

/**
 * Helper for consuming a single service.
 */
class GetOne[T](context: BundleContext, serviceInterface: Class[T]) {

  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")

  // ===========================================================================
  //  API
  // ===========================================================================

  /**
   * Applies the given function to the service.
   */
  def andApply[S](f: T => S): Option[S] = {
    require(f != null, "Function to be applied must not be null!")
    context.getServiceReference(serviceInterface.getName) match {
      case null                  => None
      case ref: ServiceReference => context.applyWithRef(ref, f)
    }
  }

  /**
   * Applies the given function to the service and its properties.
   */
  def andApplyWithProperties[S](f: (T, Map[String, AnyRef]) => S): Option[S] = {
    require(f != null, "Function to be applied must not be null!")
    context.getServiceReference(serviceInterface.getName) match {
      case null                  => None
      case ref: ServiceReference => context.applyWithRef(ref, f)
    }
  }
}
