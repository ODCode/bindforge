package org.scalamodules.di

import scala.collection.mutable.ListBuffer
import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names


/**
 * Class to hold binding information.
 *
 * Instances will store information about the types used in bindings,
 * @code{Named} annotations, etc. These information are modified trough
 * a DSL like syntax.
 */
class Binding[A <: Object](bindingConfig: BindingConfig, var fromType: Class[A])
extends Bindable[A] {
  
  var toType: Class[_ <: A] = fromType
  var id: String = _
  val provider = new PojoProvider(this)

  /**
   * Assign an ID.
   */
  def ::(id: String): Binding[A] = {
    this.id = id
    this
  }

  def -(block: => Unit): Binding[A] = {
    bindingConfig.currentBinding = this
    block
    bindingConfig.currentBinding = null
    this
  }

  def property(name: String): PropertyInjection = {
    provider.addProperty(name)
  }

  def lifecycle(init: String, destroy: String) {
    provider.setInitAndDestroy(init, destroy)
  }
  
  override def create(binder: Binder) {
    // if an ID was provided
    if (id != null) {
      val binding = binder.bind(classOf[Object]).annotatedWith(Names.named(id))
      bindTarget(binding, true)
    }
    // Standard From type binding
    val binding = binder.bind(fromType)
    bindTarget(binding, false)
  }
  
  private def bindTarget[T >: A](binding: LinkedBindingBuilder[T], forceToBinding: Boolean) {
    var bindingTargeted: ScopedBindingBuilder[A] = binding
    bindingTargeted = binding.toProvider(provider)
    bindingTargeted.asEagerSingleton()
  }
  
}
