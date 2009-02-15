/*
 * InjectorFactory.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bindforge.di

import com.google.inject.{Guice, Injector, Module}
import org.bindforge.di._


object InjectorFactory {

  def createInjector(bindingConfig: BindingConfig): Injector = {
    val module = bindingConfig.create()
    Guice.createInjector(module)
  }
    
}
