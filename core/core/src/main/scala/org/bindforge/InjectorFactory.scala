
package org.bindforge

import com.google.inject.{Guice, Injector, Module, Binder}


object InjectorFactory {

  def createInjector(config: Config): Injector = {
    createInjector(config, Array[Module](): _*)
  }

  def createInjector(config: Config, modules: Module*): Injector = {
    val module = new Module {
      def configure(binder: Binder) {
        binder.install(config.create())
        modules.foreach(binder.install(_))
      }
    }
    val inj = Guice.createInjector(module)

    // Eagerly load
    config.getBindings.foreach(b => inj.getInstance(b.mainKey))
    
    inj
  }
    
}
