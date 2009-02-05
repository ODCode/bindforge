/*
 * InjectorFactory.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.scalamodules.di

import com.google.inject.{Guice, Injector, Module}
import org.scalamodules.di._


object InjectorFactory {

  def createInjector(bindingConfig: BindingConfig): Injector = {
    val module = bindingConfig.create()
    Guice.createInjector(module)
  }
    
}
