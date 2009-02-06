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

package org.scalamodules.demo.dsl.hello.provider.internal

import java.lang.Integer
import org.osgi.framework.{BundleActivator, BundleContext}
import org.scalamodules.demo.dsl.hello.provider.HelloService
import org.scalamodules.dsl.core.RichBundleContext.fromBundleContext

class Activator extends BundleActivator {
  
  override def start(context: BundleContext) {
    
    // Register a service
    var hello = new HelloService { 
      override def hello = "Hello World!"
    }
    context addAs classOf[HelloService] theService hello

    // Register a service with properties
    var properties = Map("name"            -> "HelloService",
                         "service.ranking" -> new Integer("1"))
    var hello2 = new HelloService { 
      override def hello = "Hello World, I have got properties!" 
    }
    context addAs classOf[HelloService] withProperties properties theService hello2
  }
  
  override def stop(context: BundleContext) { // Nothing!
  }
}
