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

import scala.collection.mutable
import com.google.inject.Injector
import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.osgi.service.cm.ManagedService
import org.slf4j.LoggerFactory

import org.bindforge.common.util.jcl.Conversions._


class ManagedTarget(val obj: Object, val updateMethod: String, val injector: Injector)

class ManagedServiceImpl(pid: String) extends ManagedService {

  private val logger = LoggerFactory.getLogger(this.getClass)
  
  val targets = new mutable.HashSet[ManagedTarget]

  def updated(dict: java.util.Dictionary[_, _]) {
    if (dict == null) return
    val config = mutable.HashMap.empty[String, Any]
    javaEnumerationToIterator(dict.keys).foreach {k => 
      config(k.asInstanceOf[String]) = dict.get(k)
    }
    applyConfig(config.elements)
  }

  def applyConfig(config: Iterator[(String, Any)]) {
    targets.foreach {target =>
      val obj = target.obj
      val updateMethod = target.updateMethod
      val injector = target.injector
      
      logger.debug("Applying configuration change for PID [{}] in object [{}]", pid, obj)
      
      // create a Java dict that we can pass to the updated method later
      val javaConfig: java.util.Map[String, Object] = new java.util.HashMap[String, Object]
      config.foreach {configEntry =>
        val (key, value) = configEntry
        javaConfig.put(key, value.asInstanceOf[Object])
        val pi = new PropertyInjection(key, value)
        try {
          pi.inject(obj.getClass.asInstanceOf[Class[Any]], obj, injector)
        }
        catch {
          case e: NoSetterForPropertyException => // Ignore
        }
      }

      if (updateMethod == null) return
      val clazz = obj.getClass
      logger.debug("Calling update method [{}#{}]", clazz.getName, updateMethod)
      val method = ReflectUtils.getMethod(clazz, updateMethod)
      val mapClass = classOf[Map[_, _]]
      method.getParameterTypes.toList match {
        case mapClass :: Nil => method.invoke(obj, javaConfig)
        case Nil => method.invoke(obj, null)
        case _ => throw new Exception("Update method [" + clazz.getName + "#" + method.getName + "] does not have a supported signature. " +
                                      "Supported are:\na) No parameters\nb) 1 parameter of type 'java.util.Map[String, Object]'")
      }
    }
  }

  def addConfigurationTarget(target: Object, updateMethod: String, injector: Injector) {
    targets += new ManagedTarget(target, updateMethod, injector)
  }
  
}


object ManagedServiceStore {
  
  private val logger = LoggerFactory.getLogger(this.getClass)

  val managedServices = mutable.HashMap.empty[String, ManagedServiceImpl]
  val registrations = new mutable.ListBuffer[ServiceRegistration]

  def getManagedService(pid: String, context: BundleContext): ManagedServiceImpl = {
    managedServices.getOrElseUpdate(pid, {
        logger.debug("Creating ManagedService for PID [{}]", pid)
        val ms = new ManagedServiceImpl(pid)
        val map = Map("service.pid" -> pid)
        val registration = context.registerService(
          classOf[ManagedService].getName, ms, mapToJavaDictionary(map))
        
        registrations += registration

        ms
      })
  }

  def addConfigurationTarget(pid: String, target: Object, updateMethod: String, injector: Injector) {
    val context = injector.getInstance(classOf[BundleContext])
    val ms = getManagedService(pid, context)
    logger.debug("Adding object [{}] as configuration target for PID [{}]", target, pid)
    ms.addConfigurationTarget(target, updateMethod, injector)
  }

  def shutdown() {
    registrations.foreach {r =>
      try {
        r.unregister()
      }
      catch {
        case e: java.lang.IllegalStateException => // Service might already be unregistered. Ignore.
      }
    }
    registrations.clear()
    managedServices.clear()
  }
  
}


class ManagedServiceConfig[A <: Object](binding: Binding[A], pid: String, updateMethod: String) {

  binding.config.addShutdownLister {
    ManagedServiceStore.shutdown()
  }

  binding.addCreationCallback {(injector, instance) =>
    ManagedServiceStore.addConfigurationTarget(pid, instance, updateMethod, injector)
  }

}
