
package org.bindforge.test.testbundle


class Config extends org.bindforge.Config {

  bind [PersonService, PersonServiceImpl] spec {
    exportService("key1" -> "value1", "key2" -> "value2")
  }

}
