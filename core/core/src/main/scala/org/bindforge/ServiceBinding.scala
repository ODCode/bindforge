
package org.bindforge

import com.google.inject.{Binder, Provider}
import com.google.inject.binder.LinkedBindingBuilder
import org.ops4j.peaberry.Peaberry._
import org.ops4j.peaberry.util.Filters.ldap


class ServiceBinding[A <: Object](config: Config, bindType: Class[A])
extends Binding[A](config, bindType) {

  private var _filter: String = null

  private var peaberryProvider: Provider[A] = _

  def importService {
    // dummy method to trigger implicit conversion
  }

  def importService(filter: String) {
    _filter = filter
    this
  }

  override def bindTarget(binder: Binder, binding: LinkedBindingBuilder[Object]) {
    if (_filter != null) {
      peaberryProvider = service(bindType).filter(ldap(_filter)).single()
    }
    else {
      peaberryProvider = service(bindType).single()
    }
    binding.toProvider(peaberryProvider)
  }

}
