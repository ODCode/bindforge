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

import scala.collection.mutable.{HashMap, LinkedHashSet}
import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names
import org.osgi.framework.ServiceRegistration


abstract class Binding[A <: Object](config: Config, val bindType: Class[A]) {

  var id: String = null

  val keys = new LinkedHashSet[Key[_]]
  def mainKey = keys.toList.first
  def restKeys = if (keys.size == 0) List[Key[_]]() else keys.toList.tail

  var isNestedBinding = false

  // This value must be set to 'true' if this binding depends
  // on the value of a parent binding. For example, the ServiceExportBinding.
  // If this value is true, tha parent binding must not ask this binding for
  // an instance.
  var recursiveParentSave = true

  val provider: CallbackProvider[_] = null

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
    bindTarget(binder, binder.bind(mainKey.asInstanceOf[Key[Object]]))
    restKeys.foreach(k => binder.bind(k.asInstanceOf[Key[Object]]).to(mainKey.asInstanceOf[Key[Object]]))
  }

  def bindTarget(binder: Binder, binding: LinkedBindingBuilder[Object]) {
  }

  def addCreationCallback(callback: (Injector, A) => Unit) {
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
