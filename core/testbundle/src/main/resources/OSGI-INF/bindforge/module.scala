
package org.bindforge.di.testbundle


class BindingConfig extends org.bindforge.di.BindingConfig {

  println("Hello")

  bind [IdService] spec {
    exportService("conference" -> "EclipseCon")
  }

  println("end")

}
