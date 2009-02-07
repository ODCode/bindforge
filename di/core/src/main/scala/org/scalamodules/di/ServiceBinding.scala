/*
 * ServiceBinding.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.scalamodules.di

import com.google.inject.Binder
import com.google.inject.binder.LinkedBindingBuilder
import org.ops4j.peaberry.Peaberry._
import org.ops4j.peaberry.util.Filters.ldap


class ServiceBinding[A <: Object](bindingConfig: BindingConfig, bindType: Class[A])
extends Binding[A](bindingConfig, bindType) {

  private var _filter: String = null

  def filter(filter: String): ServiceBinding[A] = {
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
