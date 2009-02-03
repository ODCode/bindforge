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

package org.scalamodules.demo.dsl.hello.consumer.internal

import org.osgi.framework.{BundleActivator, BundleContext}
import org.scalamodules.demo.dsl.hello.provider.HelloService
import org.scalamodules.dsl.core.RichBundleContext.fromBundleContext

class Activator extends BundleActivator {

  override def start(context: BundleContext) {

    println("getOne:")
    context getOne classOf[HelloService] andApply { _.hello } match {
      case None    => println("No HelloService available!")
      case Some(s) => println(s)
    }

    println("getOne withProperties:")
    context getOne classOf[HelloService] andApplyWithProperties { 
      (helloService, properties) => properties.get("name") match {
        case None       => helloService.hello
        case Some(name) => helloService.hello + "; name=" + name
      } 
    } match {
      case None    => println("No HelloService available!")
      case Some(s) => println(s)
    }

    println("getMany:")
    context getMany classOf[HelloService] andApply { _.hello } match {
      case None         => println("No HelloServices available!")
      case Some(hellos) => hellos.foreach(println)
    }

    println("getMany withProperties:")
    context getMany classOf[HelloService] andApplyWithProperties { 
      (helloService, properties) => properties.get("name") match {
        case None       => helloService.hello
        case Some(name) => helloService.hello + "; name=" + name
      } 
    } match {
      case None         => println("No HelloServices available!")
      case Some(hellos) => hellos.foreach(println)
    }
  }

  override def stop(context: BundleContext) { // Nothing!
  }
}
