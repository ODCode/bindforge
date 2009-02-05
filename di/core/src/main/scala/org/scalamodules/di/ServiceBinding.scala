/*
 * ServiceBinding.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.scalamodules.di

import com.google.inject.Binder
import org.ops4j.peaberry.Peaberry._
import org.ops4j.peaberry.util.Filters.ldap


class ServiceBinding[A <: Object](objectClass: Class[A]) extends Bindable[A] {

  private var _filter: String = null

  def filter(filter: String): ServiceBinding[A] = {
    _filter = filter
    this
  }

  override def create(binder: Binder) {
    if (_filter != null) {
      binder.bind(objectClass).toProvider(service(objectClass).filter(ldap(_filter)).single())
    }
    else {
      binder.bind(objectClass).toProvider(service(objectClass).single())
    }

  }

}
