/*
 * ServiceBinding.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.scalamodules.di

import java.util.Hashtable
import com.google.inject.Binder
import com.google.inject.Provider
import com.google.inject.name.Names
import org.osgi.framework.ServiceRegistration


class ServiceExportBinding[A <: Object](bindingConfig: BindingConfig, bindType: Class[A], val parentBinding: PojoBinding[A])
extends Binding[A](bindingConfig, bindType) {

  val properties = new Hashtable[String, Object]
  var propertiesMethod: String = _

  override def create(binder: Binder) {
    // We have to create a name for this binding. Otherwise we would only bind to
    // the class ServiceRegistration and Guice' configuration would fail as soon as
    // a 2nd registration would get binded. Therefore, we either use the ID supplied
    // by the user or we use a unique combination of various properties
    var name = id
    if (name == null) {
      name = parentBinding.bindType.getName + parentBinding.toType.getName + parentBinding.id
    }
    val provider = new ServiceRegistrationProvider(this, bindType.getName, parentBinding.key)
    binder.bind(classOf[ServiceRegistration]).annotatedWith(Names.named(name)).toProvider(provider).asEagerSingleton()
  }

  def properties(dict: Tuple2[String, Object]*) {
    dict.foreach(e => properties.put(e._1, e._2))
  }

  def method(name: String) {
    propertiesMethod = name
  }



}