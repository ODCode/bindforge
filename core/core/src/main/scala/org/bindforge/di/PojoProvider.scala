package org.bindforge.di

import scala.collection.mutable.ListBuffer
import java.lang.reflect.Method
import com.google.inject._
import com.google.inject.name._


trait InjectionPoint {
  def inject[A](clazz: Class[A], obj: A, injector: Injector)
}

class PropertyInjection(name: String) extends InjectionPoint {
  
  var ref: String = _
  var _value: Any = _
  
  def ref(ref: String): PropertyInjection = {
    this.ref = ref
    this
  }

  def value(value: Any) {
    _value = value
  }
  
  def inject[A](clazz: Class[A], obj: A, injector: Injector) {
    // First search for a "normal" setter method
    var setterName = "set" + name.toList.head.toUpperCase + name.toList.tail
    var methods = clazz.getMethods.filter(_.getName == setterName)
    // If none was found, check for a "scala-like" setter method
    if (methods.length == 0) {
      setterName = name + "_$eq"
      methods = clazz.getMethods.filter(_.getName == setterName)
    }
    val setMethod: Method = methods(0)

    if (_value == null) {
      val paramType = setMethod.getParameterTypes()(0)
      val key = if (ref == null) Key.get(paramType) else Key.get(classOf[Object], Names.named(ref))
      val paramValue = injector.getInstance(key).asInstanceOf[Object]
      setMethod.invoke(obj, paramValue)
    }
    else {
      setMethod.invoke(obj, _value.asInstanceOf[Object])
    }
  }
}

class PojoProvider[A <: Object](binding: PojoBinding[A]) extends Provider[A] {
  
  @Inject
  var injector: Injector = _

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
      if (m == null) {
        throw new IllegalStateException("Method with name [" + methodName + "] is not defined in class [" + binding.toType.getName + "]")
      }
      else {
        return m
      }
    }
    return null
  }

  def addProperty(name: String): PropertyInjection = {
    val p = new PropertyInjection(name)
    injectionPoints += p
    p
  }

  def get(): A = {
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
