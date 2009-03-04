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
import java.lang.reflect.Method
import org.slf4j.LoggerFactory
import com.google.inject._
import com.google.inject.name._


class NoSetterForPropertyException(clazz: Class[_], name: String)
extends Exception("No setter method for property [" + clazz.getName + "#" + name + "] was found")


trait InjectionPoint {
  def inject[A](clazz: Class[A], obj: A, injector: Injector)
}

class PropertyInjection(name: String) extends InjectionPoint {

  private val logger = LoggerFactory.getLogger(this.getClass)
  
  var _ref: String = _
  var _value: Any = _
  
  def ref(ref: String): PropertyInjection = {
    _ref = ref
    this
  }

  def value(value: Any): PropertyInjection = {
    _value = value
    this
  }

  def inject[A](clazz: Class[A], obj: A, injector: Injector) {
    // First search for a "normal" setter method
    var setterName = ("set" :: name.toList.head.toUpperCase :: name.toList.tail).mkString
    var methods = clazz.getMethods.filter(_.getName == setterName)
    // If none was found, check for a "scala-like" setter method
    if (methods.length == 0) {
      setterName = name + "_$eq"
      methods = clazz.getMethods.filter(_.getName == setterName)
    }
    if (methods.length == 0) {
      throw new NoSetterForPropertyException(clazz, name)
    }
    val setMethod: Method = methods(0)
    logger.debug("Injecting [{}] with method [{}]", clazz.getName + "#" + name, setterName)

    // find the value to inject
    var injectValue: Object = if (_value != null) {
      _value match {
        // if a binding was specified, register a callback to get the value
        // of this binding's provider as soon as it is available. This is required
        // to avoid a circular reference between this provider and the specified binding.
        // For example, this would happen if an export handle gets injected in the
        // binding that is going to be exported
        case b: Binding[A] => {
            val pi = new PropertyInjection(name)
            b.addCreationCallback {(inj: Injector, instance: A) =>
              pi.value(instance)
              pi.inject(clazz, obj, injector)
            }
            // Skip the injection for now. Will happen in the callback.
            return
          }
          // Use the specified value
        case _ => _value.asInstanceOf[Object]
      }
    }
    else {
      val paramType = setMethod.getParameterTypes()(0)
      val key = if (_ref == null) Key.get(paramType) else Key.get(classOf[Object], Names.named(_ref))
      injector.getInstance(key).asInstanceOf[Object]
    }

    // inject the value
    setMethod.invoke(obj, injectValue)
  }
}

class PojoProvider[A <: Object](binding: PojoBinding[A]) extends Provider[A] {
  
  private val injectionPoints = new ListBuffer[InjectionPoint]

  private var initMethod: Method = _
  private var destroyMethod: Method = _
  private var instance: A = _

  def setInitAndDestroy(init: String, destroy: String) {
    initMethod = getMethod(init, "init")
    destroyMethod = getMethod(destroy, "destroy")
  }

  def getMethod(methodName: String, defaultName: String): Method = {
    if (methodName != null) {
      val m = ReflectUtils.getMethod(binding.toType, methodName)
      return m
    }
    return null
  }

  def addProperty(name: String): PropertyInjection = {
    val p = new PropertyInjection(name)
    injectionPoints += p
    p
  }

  def getInstance(): A = {
    if (instance == null) {
      create()
    }
    instance
  }

  def create() {
    val obj = binding.toType.newInstance

    // Default Guice injection mechanism
    injector.injectMembers(obj)

    // bindforge DI mechanism
    injectionPoints.foreach(_.inject(binding.bindType, obj, injector))

    // if defined, execute init method
    if (initMethod != null) {
      executeInitMethod(obj)
    }

    instance = obj
  }

  def executeInitMethod(obj: A) {
    initMethod.invoke(obj)
  }

}
