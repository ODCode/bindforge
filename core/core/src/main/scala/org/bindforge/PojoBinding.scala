
package org.bindforge

import scala.collection.mutable.ListBuffer
import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names
import org.osgi.framework.ServiceRegistration


/**
 * Class to hold binding information.
 *
 * Instances will store information about the types used in bindings,
 * @code{Named} annotations, etc. These information are modified with
 * a DSL like syntax.
 */
class PojoBinding[A <: Object](config: Config, bindType: Class[A])
extends Binding[A](config, bindType) {
  
  var toType: Class[_ <: A] = bindType
  val provider = new PojoProvider(this)

  private var exportServiceDict: Map[String, Object] = null

  override def spec(block: => Unit) {
    config.currentBinding = this
    block
    config.currentBinding = null
    this
  }

  override def bindTarget[T >: A](binder: Binder, binding: LinkedBindingBuilder[T]) {
    binding.toProvider(provider).asEagerSingleton()
  }

  override def property(name: String): PropertyInjection = {
    provider.addProperty(name)
  }

  override def lifecycle(init: String, destroy: String) {
    provider.setInitAndDestroy(init, destroy)
  }
  
  override def exportService(dict: Tuple2[String, Object]*) {
    val seb = new ServiceExportBinding(config, bindType, this)
    seb.properties(dict: _*)
    nestedBindings += seb
  }
  
}
