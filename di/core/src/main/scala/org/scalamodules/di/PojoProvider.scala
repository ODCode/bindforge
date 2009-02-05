package org.scalamodules.di

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

class PojoProvider[A <: Object](clazz: Class[A], injectionPoints: Seq[InjectionPoint]) extends Provider[A] {
  
  @Inject
  var injector: Injector = _
  
  def get(): A = {
    val obj = clazz.newInstance

    // Default Guice injection mechanism
    injector.injectMembers(obj)

    // ScalaModules DI mechanism
    injectionPoints.foreach(_.inject(clazz, obj, injector))
       
    obj
  }

}
