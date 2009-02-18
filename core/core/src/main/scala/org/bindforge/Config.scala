
package org.bindforge

import scala.collection.mutable.ListBuffer
import scala.reflect.Manifest
import com.google.inject._
import com.google.inject.binder._


class Config {
  
  val bindings = new ListBuffer[Binding[_]]
  var currentBinding: Binding[_] = _

  def create(): Module = new Module() {
    def configure(binder: Binder) {
      bindings.foreach(_.create(binder))
    }
  }
  
  def bind[A <: Object](implicit fromType: Manifest[A]): PojoBinding[A] = bind(fromType, null)

  def bind[A <: Object, B <: A](implicit fromType: Manifest[A], toType: Manifest[B]): PojoBinding[A] = {
    val b = new PojoBinding(this, fromType.erasure.asInstanceOf[Class[A]])
    if (toType != null) {
      b.toType = toType.erasure.asInstanceOf[Class[B]]
    }
    bindings += b
    b
  }

  implicit def binding2serviceImport[A <: Object](binding: Binding[A]): ServiceBinding[A] = {
    bindings.remove(bindings.indexOf(binding))
    val newB = new ServiceBinding(this, binding.bindType)
    bindings += newB
    newB
  }

  def lifecycle(init: String) {
    lifecycle(init, null)
  }

  def lifecycle(init: String, destroy: String) {
    currentBinding.lifecycle(init, destroy)
  }

  def property(name: String): PropertyInjection = {
    currentBinding.property(name)
  }

  def exportService(dict: Tuple2[String, Object]*) {
    currentBinding.exportService(dict: _*)
  }

}

