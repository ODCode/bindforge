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

import scala.collection.mutable.ListBuffer
import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names


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

  def exportService(dict: Tuple2[String, Object]*): ServiceExportBinding[A] = {
    throw new IllegalStateException("Setting 'exportService' not valid for bindings of type [" + this.getClass + "]")
  }

}
