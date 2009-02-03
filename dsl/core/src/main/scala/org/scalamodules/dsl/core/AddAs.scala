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

import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.scalamodules.common.util.jcl.Conversions.mapToJavaDictionary

/**
 * Helper for adding services.
 */
class AddAs[T](context: BundleContext, serviceInterface: Class[T],
               properties: Map[String, Any]) {

  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")

  def this(context: BundleContext, serviceInterface: Class[T]) {
    this(context, serviceInterface, null)
  }

  // ===========================================================================
  //  API
  // ===========================================================================

  /**
   * Creates an AddAs with the given service properties.
   */
  def withProperties(properties: Map[String, Any]): AddAs[T] =
    new AddAs(context, serviceInterface, properties)

  /**
   * Adds the given service.
   */
  def theService(service: T): ServiceRegistration = {
    require(service != null, "Service must not be null!")
    context.registerService(serviceInterface.getName, service, properties)
  }
}
