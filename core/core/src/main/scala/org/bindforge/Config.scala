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

import scala.collection.mutable.{ListBuffer, HashMap, Stack}
import scala.reflect.Manifest
import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names


class Config {

  val modules = new ListBuffer[Module]

  def install(m: Module) {
    modules += m
  }

  val bindings = new ListBuffer[Binding[_ <: Object]]
  //val typeCounter = new HashMap[Class[_ <: Object], Int]

  val specStack = new Stack[Binding[_ <: Object]]
  
  def currentBinding: Binding[_ <: Object] = {
    specStack.top
  }

  def addBinding(b: Binding[_ <: Object]) {
    bindings += b
  }

  def removeBinding(b: Binding[_ <: Object]) {
    bindings.remove(bindings.indexOf(b))
  }

  /**
   * Generate unique keys for all bindings.
   *
   * Implementation detail: The order the generated keys are very important.
   * If done wrong, the keys could point to itself with an endless loop. Modifications
   * to this method should therefore be done very carefully.
   */
  def generateKeysForBindings() {
    // check for each binding....
    // (only if it is not a nested binding since nested bindings should never be visible only by their type)
    bindings.filter(!_.isNestedBinding).foreach {b =>
      // ... if it is the only binding for this type
      val sameTypeBindings = bindings.filter(that => that != b && that.bindType == b.bindType)
      if (sameTypeBindings.length == 0) b.keys += Key.get(b.bindType)

      // ... if it is the only binding with no ID
      val alsoNoIdBindings = bindings.filter(that => that != b && that.bindType == b.bindType && that.id == null)
      if (alsoNoIdBindings.length == 0 && b.id == null) b.keys += Key.get(b.bindType)
    }

    // generate (Object, ID) keys for all bindings with an ID
    bindings.filter(_.id != null).foreach(b => b.keys += Key.get(classOf[Object], Names.named(b.id)))

    // generate an ID for bindings that still do not have a key
    bindings.filter(_.keys.size == 0).foreach {b =>
      val id = "binding_" + b + "_hash_" + b.hashCode
      b.keys += Key.get(b.bindType, Names.named(id))
    }

  }

  def create(): Module = {
    new Module() {
      generateKeysForBindings()
      def configure(binder: Binder) {
        modules.foreach(binder.install)
        bindings.foreach(_.create(binder))
      }
    }
  }

  def shutdown() {
    bindings.foreach(_.shutdown())
  }

  def bind[A <: Object](implicit fromType: Manifest[A]): Binding[A] = bind(fromType, null)

  def bind[A <: Object, B <: A](implicit fromType: Manifest[A], toType: Manifest[B]): Binding[A] = {
    val from: Class[A] = fromType.erasure.asInstanceOf[Class[A]]

    var newBinding: PojoBinding[A] = null
    if (toType == null) {
      newBinding = new PojoBinding(this, from)
    }
    else {
      newBinding = new PojoBinding[A](this, from, toType.erasure.asInstanceOf[Class[B]])
    }
    // if this is a nested binding, create a unique ID by pairing the parent binding and this binding
    if (!specStack.isEmpty) {
      newBinding.id = "nested_binding: parent_" + specStack.top.hashCode + "_nested_" + newBinding.hashCode
      newBinding.isNestedBinding = true
    }

    addBinding(newBinding)
    //increaseTypeCounter(newBinding)
    newBinding
  }

  implicit def binding2serviceImport[A <: Object](binding: Binding[A]): ServiceBinding[A] = {
    removeBinding(binding)
    //decreaseTypeCounter(binding)
    val newB = new ServiceBinding(this, binding.bindType)
    addBinding(newB)
    //increaseTypeCounter(newB)
    newB
  }

  def lifecycle(init: String) {
    lifecycle(init, null)
  }

  def lifecycle(init: String, destroy: String) {
    currentBinding.lifecycle(init, destroy)
  }

  def ref(name: String): Symbol = {
    Symbol(name)
  }

  object property {
    def apply(name: String) {
      currentBinding.property(name, InjectWithType)
    }
    def update(name: String, value: Any) {
      currentBinding.property(name, value)
    }
  }

  def exportService: ServiceExportBinding = {
    // pass empty list
    currentBinding.exportService(List[Tuple2[String, Object]](): _*)
  }

  def exportService(dict: Tuple2[String, Object]*): ServiceExportBinding = {
    currentBinding.exportService(dict: _*)
  }

  def config(pid: String) {
    config(pid, null)
  }

  def config(pid: String, updateMethod: String) {
    new ManagedServiceConfig(currentBinding, pid, updateMethod)
  }

}

