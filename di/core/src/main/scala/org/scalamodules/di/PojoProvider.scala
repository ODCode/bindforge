package org.scalamodules.di

import scala.collection.mutable.ListBuffer
import java.lang.reflect.Method
import com.google.inject._
import com.google.inject.name._


trait InjectionPoint {
  def inject[A](clazz: Class[A], obj: A, injector: Injector)
}

class PropertyInjection(name: String) extends InjectionPoint {
  
  var ref: String = _
  
  def <--(ref: String): PropertyInjection = {
    this.ref = ref
    this
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
        
    val paramType = setMethod.getParameterTypes()(0)
    val key = if (ref == null) Key.get(paramType) else Key.get(classOf[Object], Names.named(ref))
    val paramValue = injector.getInstance(key).asInstanceOf[Object]
    
    setMethod.invoke(obj, paramValue)
  }
}

class PojoProvider[A <: Object](binding: Binding[A]) extends Provider[A] {
  
  @Inject
  var injector: Injector = _

  private val injectionPoints = new ListBuffer[InjectionPoint]

  var _initMethod: String = _
  var _destroyMethod: String = _

  def setInitAndDestroy(init: String, destroy: String) {
    _initMethod = init
    _destroyMethod = destroy
  }

  def addProperty(name: String): PropertyInjection = {
    val p = new PropertyInjection(name)
    injectionPoints += p
    p
  }

  def get(): A = {
    val obj = binding.toType.newInstance

    // Default Guice injection mechanism
    injector.injectMembers(obj)

    // ScalaModules DI mechanism
    injectionPoints.foreach(_.inject(binding.fromType, obj, injector))

    if (_initMethod != null) {
      executeInitMethod(obj)
    }
       
    obj
  }

  def executeInitMethod(obj: A) {
  }

}
