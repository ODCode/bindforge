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
import org.osgi.framework.BundleContext
import org.osgi.service.cm.ManagedService


class ManagedServiceImpl extends ManagedService {

  def updated(dict: java.util.Dictionary[_, _]) {
    println("got new config: " + dict)
  }
  
}

object ManagedServices {
  def abc = ""
}


class ManagedServiceConfig[A <: Object](binding: Binding[A], pid: String) {

  binding.addCreationCallback {(injector, instance) =>
    val context = injector.getInstance(classOf[BundleContext])


    println("OOOO " + context + "PID: " + pid)
  }

}
