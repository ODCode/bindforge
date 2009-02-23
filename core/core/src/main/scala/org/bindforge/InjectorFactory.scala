
package org.bindforge

import com.google.inject.{Guice, Injector, Module, Binder}


object InjectorFactory {

  def createInjector(config: Config): Injector = {
    val module = config.create()
    Guice.createInjector(module)
  }

  def createInjector(config: Config, modules: Module*): Injector = {
    val module = new Module {
      def configure(binder: Binder) {
        binder.install(config.create())
        modules.foreach(binder.install(_))
      }
    }
    val i = Guice.createInjector(module)
    println("////////////////////////////////////////////////////////////////////")
    println(i)
    println("////////////////////////////////////////////////////////////////////")
    i
  }
    
}
