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
import scala.reflect.Manifest
import com.google.inject._
import com.google.inject.binder._


class Config {
  
  val bindings = new ListBuffer[Binding[_]]
  
  var currentBinding: Binding[_ <: Object] = _

  def create(): Module = new Module() {
    def configure(binder: Binder) {
      bindings.foreach(_.create(binder))
    }
  }
  
  def bind[A <: Object](implicit fromType: Manifest[A]): PojoBinding[A] = bind(fromType, null)

  def bind[A <: Object, B <: A](implicit fromType: Manifest[A], toType: Manifest[B]): PojoBinding[A] = {
    val from: Class[A] = fromType.erasure.asInstanceOf[Class[A]]

    var newBinding: PojoBinding[A] = null
    if (toType == null) {
      newBinding = new PojoBinding[A](this, from)
    }
    else {
      newBinding = new PojoBinding[A](this, from, toType.erasure.asInstanceOf[Class[B]])
    }
    bindings += newBinding
    newBinding
  }

  implicit def binding2serviceImport[A <: Object](binding: Binding[A]): ServiceBinding[A] = {
    bindings.remove(bindings.indexOf(binding))
    val newB = new ServiceBinding(this, binding.bindType)
    bindings += newB
    newB
  }

  def lifecycle(init: String) {
    lifecycle(init, null)
  }

  def lifecycle(init: String, destroy: String) {
    currentBinding.lifecycle(init, destroy)
  }

  def property(name: String): PropertyInjection = {
    currentBinding.property(name)
  }

  def exportService: ServiceExportBinding[_] = {
    // pass empty list
    currentBinding.exportService(List[Tuple2[String, Object]](): _*)
  }

  def exportService(dict: Tuple2[String, Object]*): ServiceExportBinding[_] = {
    currentBinding.exportService(dict: _*)
  }

  def config(pid: String) {
    config(pid, null)
  }

  def config(pid: String, updateMethod: String) {
    new ManagedServiceConfig(currentBinding, pid, updateMethod)
  }


}

