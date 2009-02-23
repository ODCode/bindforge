
package org.bindforge

import com.google.inject.Binder
import com.google.inject.binder.LinkedBindingBuilder
import org.ops4j.peaberry.Peaberry._
import org.ops4j.peaberry.util.Filters.ldap


class ServiceBinding[A <: Object](config: Config, bindType: Class[A])
extends Binding[A](config, bindType) {

  private var _filter: String = null

  override val provider: Provider[A] = null

  def importService {
    // dummy method to trigger implicit conversion
  }

  def importService(filter: String) {
    _filter = filter
    this
  }

  override def bindTarget[T >: A](binder: Binder, binding: LinkedBindingBuilder[T]) {
    if (_filter != null) {
      binding.toProvider(service(bindType).filter(ldap(_filter)).single())
    }
    else {
      binding.toProvider(service(bindType).single())
    }
  }

}
