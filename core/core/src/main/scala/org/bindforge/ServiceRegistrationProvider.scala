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

import org.osgi.framework.{BundleContext, ServiceRegistration}
import com.google.inject.{Inject, Injector, Key}


class ServiceRegistrationProvider(exportBinding: ServiceExportBinding) extends Provider[ServiceRegistration] {

  @Inject
  var context: BundleContext = _

  private var serviceRegistration: ServiceRegistration = _

  private def registerService() {
    val obj = injector.getInstance(exportBinding.parentBinding.key)
    val props = exportBinding.properties
    serviceRegistration = context.registerService(exportBinding.parentBinding.bindType.getName, obj, props)
  }

  def getInstance(): ServiceRegistration = {
    if (serviceRegistration == null) registerService()
    serviceRegistration
  }

}
