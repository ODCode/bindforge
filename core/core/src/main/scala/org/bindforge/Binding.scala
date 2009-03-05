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

package org.bindforge

import scala.collection.mutable.{HashMap, ListBuffer}
import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names
import org.osgi.framework.ServiceRegistration


abstract class Binding[A <: Object](config: Config, val bindType: Class[A]) {

  type SelfType <: Binding[A]

  var id: String = null

  var key: Key[_ <: Object] = null

  val nestedBindings = new ListBuffer[Binding[_ <: Object]]

  val provider: Provider[_] = null

  /**
   * Assign an ID.
   */
  def ::(id: String): SelfType = {
    this.id = id
    this.asInstanceOf[SelfType]
  }

  def create(binder: Binder) {
    /*
     // if an ID was provided
     if (id != null) {
     val binding = binder.bind(classOf[Object]).annotatedWith(Names.named(id))
     key = Key.get(classOf[Object], Names.named(id))
     bindTarget(binder, binding)
     }
     else {
     // Standard From type binding
     val binding = binder.bind(bindType)
     key = Key.get(bindType)
     bindTarget(binder, binding)
     }
     */

    // if an ID was specified, bind to (Object, ID)
    println("... >>>>>>>>>>>>")
    println("... for Binding: " + this.bindType + " with ID: " + this.id)
    println("... ...")
    if (id != null) {
      val binding = binder.bind(classOf[Object]).annotatedWith(Names.named(id))
      bindTarget(binder, binding)
      key = Key.get(classOf[Object], Names.named(id))

      if (key != null) println("... Binded with key " + key)
    }
    // if only one binding for this type was specified, bind directly to this type
    if (config.typeCounter(bindType) == 1) {
      val binding = binder.bind(bindType)
      bindTarget(binder, binding)
      key = Key.get(bindType)

      if (key != null) println("... Binded with key " + key)
    }
    // if non of the above "rules" applied, bind and generate a unique ID
    if (key == null) {
      id = bindType.getClass.getName + this.hashCode.toString
      val binding = binder.bind(classOf[Object]).annotatedWith(Names.named(id))
      bindTarget(binder, binding)
      key = Key.get(bindType, Names.named(id))

      if (key != null) println("... Binded with key " + key)
    }
    println("... to Provider: " + provider)
    println("... ...")
    println("... Binding list: " + config.bindings.map(_.bindType.getName).mkString(";"))
    println("... No of same type: " + config.typeCounter(bindType))
    println("... <<<<<<<<<<<<")
    

    nestedBindings.foreach(_.create(binder))
  }

  def addCreationCallback(callback: (Injector, A) => Unit) {
    // ugly warp to get from "Any" to "A"
    provider.addCreationCallback((injector, obj) => callback(injector, obj.asInstanceOf[A]))
  }

  def bindTarget[T >: A](binder: Binder, binding: LinkedBindingBuilder[T]) {
  }

  def spec(block: => Unit) {
    throw new IllegalStateException("Block settings are not valid for bindings of type [" + this.getClass + "]")
  }

  def property(name: String): PropertyInjection = {
    throw new IllegalStateException("Setting 'property' not valid for bindings of type [" + this.getClass + "]")
  }

  def lifecycle(init: String, destroy: String) {
    throw new IllegalStateException("Setting 'lifecycle' not valid for bindings of type [" + this.getClass + "]")
  }

  def exportService(dict: Tuple2[String, Object]*): ServiceExportBinding = {
    throw new IllegalStateException("Setting 'exportService' not valid for bindings of type [" + this.getClass + "]")
  }

}
