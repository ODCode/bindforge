package org.bindforge.di

import scala.collection.mutable.ListBuffer
import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names


abstract class Binding[A <: Object](bindingConfig: BindingConfig, val bindType: Class[A]) {

  var id: String = null

  var key: Key[_ <: Object] = null

  val nestedBindings = new ListBuffer[Binding[_ <: Object]]

  /**
   * Assign an ID.
   */
  def ::(id: String): Binding[A] = {
    this.id = id
    this
  }

  def create(binder: Binder) {
    // if an ID was provided
    if (id != null) {
      val binding = binder.bind(classOf[Object]).annotatedWith(Names.named(id))
      key = Key.get(classOf[Object], Names.named(id))
      bindTarget(binder, binding)
    }
    else {
      // Standard From type binding
      val binding = binder.bind(bindType)
      key = Key.get(bindType)
      bindTarget(binder, binding)
    }

    nestedBindings.foreach(_.create(binder))
  }

  def bindTarget[T >: A](binder: Binder, binding: LinkedBindingBuilder[T]) {
    
  }

  def spec(block: => Unit) {
    throw new IllegalStateException("Block settings are not valid for bindings of type [" + this.getClass + "]")
  }

  def property(name: String): PropertyInjection = {
    throw new IllegalStateException("Setting 'property' not valid for bindings of type [" + this.getClass + "]")
  }

  def lifecycle(init: String, destroy: String) {
    throw new IllegalStateException("Setting 'lifecycle' not valid for bindings of type [" + this.getClass + "]")
  }

  def exportService(dict: Tuple2[String, Object]*) {
    throw new IllegalStateException("Setting 'exportService' not valid for bindings of type [" + this.getClass + "]")
  }

}
