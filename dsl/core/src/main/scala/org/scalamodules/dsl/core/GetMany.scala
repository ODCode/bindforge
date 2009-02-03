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
 * Helper for consuming multiple services.
 */
class GetMany[T](context: BundleContext, serviceInterface: Class[T],
                 filter: String) {

  require(context != null, "Bundle context must not be null!")
  require(serviceInterface != null, "Service interface must not be null!")

  def this(context: BundleContext, serviceInterface: Class[T]) {
    this(context, serviceInterface, null)
  }

  // ===========================================================================
  //  API
  // ===========================================================================

  /**
   * Creates a GetMany with the given filter.
   */
  def withFilter(filter: String): GetMany[T] =
    new GetMany(context, serviceInterface, filter)

  /**
   * Applies the given function to each service.
   */
  def andApply[S](f: T => S): Option[List[S]] = {
    work(context.applyWithRef(_, f))
  }

  /**
   * Applies the given function to each service and its properties.
   */
  def andApplyWithProperties[S](f: (T, Map[String, AnyRef]) => S): Option[List[S]] = {
    work(context.applyWithRef(_, f))
  }

  // ===========================================================================
  //  NON-API
  // ===========================================================================

  def work[S](f: ServiceReference => Option[S]): Option[List[S]] = {
    require(f != null, "Function to be applied must not be null!")
    var result: List[S] = Nil
    context.getServiceReferences(serviceInterface.getName, filter) match {
      case null                          => result
      case refs: Array[ServiceReference] => refs.foreach { 
        f(_) match {
          case None    => result
          case Some(s) => result = s :: result
        }
      }  
    }
    result.size match {
      case 0 => None
      case _ => Some(result)
    }
  }
}
