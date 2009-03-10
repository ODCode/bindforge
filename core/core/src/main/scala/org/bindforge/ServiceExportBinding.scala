/*°
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

import java.util.Hashtable
import com.google.inject.Binder
import com.google.inject.Key
import com.google.inject.Provider
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.name.Names
import org.osgi.framework.ServiceRegistration


class ServiceExportBinding(config: Config, val parentBinding: PojoBinding[_])
extends Binding(config, classOf[ServiceRegistration]) {

  override val provider = new ServiceRegistrationProvider(this)
  
  isNestedBinding = true

  recursiveParentSave = false

  // Start with a unique ID
  this.id = getClass.getName + hashCode + "_forBinding_" + parentBinding.hashCode

  val properties = new Hashtable[String, Object]

  override def bindTarget(binder: Binder, binding: LinkedBindingBuilder[Object]) {
    binding.toProvider(provider).asEagerSingleton()
  }

  def properties(dict: Tuple2[String, Object]*) {
    dict.foreach(e => properties.put(e._1, e._2))
  }

}
