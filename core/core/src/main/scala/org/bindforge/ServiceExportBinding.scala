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
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.name.Names
import org.osgi.framework.ServiceRegistration


class ServiceExportBinding(config: Config, val parentBinding: PojoBinding[_])
extends Binding(config, classOf[ServiceRegistration]) {

  override type SelfType = ServiceExportBinding

  override val provider = new ServiceRegistrationProvider(this)

  val properties = new Hashtable[String, Object]

  /*
  override def create(binder: Binder) {
    // We have to create a name for this binding. Otherwise we would only bind to
    // the class ServiceRegistration and Guice' configuration would fail as soon as
    // a 2nd registration would get binded. Therefore, we either use the ID supplied
    // by the user or we use a unique combination of various properties
    var name = id
    if (name == null) {
      name = parentBinding.bindType.getName + parentBinding.toType.getName + parentBinding.id + this.hashCode
    }
    provider.key = parentBinding.key
    binder.bind(classOf[Object]).annotatedWith(Names.named(name)).toProvider(provider).asEagerSingleton()

    // Since we override create() we have to assign the key ourself
    key = Key.get(classOf[Object], Names.named(name))
  }
  */

  override def bindTarget[ServiceRegistration](binder: Binder, binding: LinkedBindingBuilder[ServiceRegistration]) {
    // I have no idea why I have to cast the provider to Provider[ServiceRegistration]
    // since the ServiceRegistrationProvider already extends Provider[ServiceRegistration]
    // However, it is working...
    binding.toProvider(provider.asInstanceOf[Provider[ServiceRegistration]]).asEagerSingleton()
  }


  def properties(dict: Tuple2[String, Object]*) {
    dict.foreach(e => properties.put(e._1, e._2))
  }

}
