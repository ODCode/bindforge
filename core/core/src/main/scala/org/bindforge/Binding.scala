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

  var id: String = null

  var key: Key[_] = null

  val nestedBindings = new ListBuffer[Binding[_ <: Object]]

  val provider: Provider[_] = null

  /**
   * Assign an ID.
   */
  def ::(id: String): this.type = {
    this.id = id
    this
  }

  def beforeBind(binder: Binder) {
  }

  def create(binder: Binder) {
    beforeBind(binder)
    
    println("... >>>>>>>>>>>>")
    println("... for Binding: " + this.bindType + " with ID: " + this.id)
    println("... ...")
    // if only one binding for this type was specified, bind directly to this type
    if (config.typeCounter(bindType) == 1) {
      key = Key.get(bindType)
      bindTarget(binder, binder.bind(key).asInstanceOf[LinkedBindingBuilder[Object]])
      if (key != null) println("... Binded with key (only one binding) " + key)
    }
    // if an ID was specified, bind to (Object, ID)
    if (id != null) {
      val newKey = Key.get(classOf[Object], Names.named(id))
      // if a binding already exists, bind to the existing key
      if (key != null) {
        binder.bind(newKey).to(key.asInstanceOf[Key[Object]])
      }
      // if this is the first binding
      else {
        key = newKey
        bindTarget(binder, binder.bind(key).asInstanceOf[LinkedBindingBuilder[Object]])
      }
      if (key != null) println("... Binded with key (specified) " + key)
    }
    // if non of the above "rules" applied, bind and generate a unique ID
    if (key == null) {
      id = bindType.getName + "_" + this.hashCode.toString
      key = Key.get(bindType, Names.named(id))
      bindTarget(binder, binder.bind(key).asInstanceOf[LinkedBindingBuilder[Object]])
      if (key != null) println("... Binded with key (generated) " + key)
    }
    println("... to Provider: " + provider)
    println("... ...")
    println("... Binding list: " + config.bindings.map(_.bindType.getName).mkString(";"))
    println("... No of same type: " + config.typeCounter(bindType))
    println("... <<<<<<<<<<<<")
    

    nestedBindings.foreach(_.create(binder))
  }

  def bindTarget(binder: Binder, binding: LinkedBindingBuilder[Object]) {
  }

  def addCreationCallback(callback: (Injector, A) => Unit) {
    println("Binding: " + this + ": Adding callback")

    // ugly warp to get from "Any" to "A"
    provider.addCreationCallback((injector, obj) => callback(injector, obj.asInstanceOf[A]))
  }

  def spec(block: => Unit): Binding[A] = {
    throw new IllegalStateException("Block settings are not valid for bindings of type [" + this.getClass + "]")
  }

  def property(name: String, value: Any) {
    throw new IllegalStateException("Setting 'property' not valid for bindings of type [" + this.getClass + "]")
  }

  def lifecycle(init: String, destroy: String) {
    throw new IllegalStateException("Setting 'lifecycle' not valid for bindings of type [" + this.getClass + "]")
  }

  def exportService(dict: Tuple2[String, Object]*): ServiceExportBinding = {
    throw new IllegalStateException("Setting 'exportService' not valid for bindings of type [" + this.getClass + "]")
  }

}
