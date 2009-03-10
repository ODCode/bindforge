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
import org.osgi.framework.ServiceRegistration


/**
 * Class to hold binding information.
 *
 * Instances will store information about the types used in bindings,
 * @code{Named} annotations, etc. These information are modified with
 * a DSL like syntax.
 */
class PojoBinding[A <: Object](config: Config, bindType: Class[A], val toType: Class[_ <: A])
extends Binding[A](config, bindType) {

  def this(config: Config, bindType: Class[A]) = {
    this(config, bindType, bindType)
  }

  override val provider = new PojoProvider(this)

  private var exportServiceDict: Map[String, Object] = null

  override def spec(block: => Unit): Binding[A] = {
    config.specStack.push(this)
    block
    config.specStack.pop
    this
  }

  override def bindTarget(binder: Binder, binding: LinkedBindingBuilder[Object]) {
    // The PojoProvider is not binded itself. Therefore, we have to request the
    // injection so that the provider has access to e.g. the injector
    binder.requestInjection(provider)

    // bind to a target if necessary
    val targeted = if (bindType != toType) {
      binding.to(toType)
    }
    else {
      binding
    }
    // bind in the scope
    targeted.in(new ProviderChainScope(provider))
  }

  override def property(name: String, value: Any) {
    provider.addProperty(name, value)
  }

  override def lifecycle(init: String, destroy: String) {
    provider.setInitAndDestroy(init, destroy)
  }
  
  override def exportService(dict: Tuple2[String, Object]*): ServiceExportBinding = {
    val seb = new ServiceExportBinding(config, this)
    config.increaseTypeCounter(seb)
    seb.properties(dict: _*)
    nestedBindings += seb
    seb
  }
  
}
