
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
    println("///////////////////////////////////////////////////////////")
    println("Injector:")
    println(inj)
    println("Activating all bindings:")
    config.bindings.foreach(b => inj.getInstance(b.mainKey))
    println("Done")
    inj
  }
    
}
