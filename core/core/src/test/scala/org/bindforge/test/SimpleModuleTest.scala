package org.bindforge.test

import com.google.inject._
import com.google.inject.name._
import org.junit.Test
import org.junit.Assert._
import org.osgi.service.packageadmin.PackageAdmin


class SimpleModuleTest {

  /*
  @Test
  def testSimpleBind() {
    class SimpleModule extends Config {
      bind [MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    
    val myi = i.getInstance(Key.get(classOf[MyServiceImpl]))
    assertTrue(myi != null)
  }


  @Test
  def testFromAndToType() {
    class SimpleModule extends Config {
      bind [MyService, MyServiceImpl]
    }
     
    val i = InjectorFactory.createInjector(new SimpleModule())
    
    val ser = i.getInstance(Key.get(classOf[MyService]))
    assertTrue(ser != null)
  }

  @Test
  def testId() {
    class SimpleModule extends Config {
      "s1" :: bind [MyServiceImpl]
    }
    val m = new SimpleModule().create()
    val i = Guice.createInjector(m)

    val ser = i.getInstance(Key.get(classOf[Object], Names.named("s1")))
    assertTrue(ser != null)
  }

  @Test
  def testInjectValue() {
    class SimpleModule extends Config {
      bind [ServiceWithProperties] spec {
        property("intp") = 123
        property("stringp") = "abc"
        property("listp") = new java.util.ArrayList[String]()
      }
    }
    val m = new SimpleModule().create()
    val i = Guice.createInjector(m)

    val ser = i.getInstance(Key.get(classOf[ServiceWithProperties]))
    assertTrue(ser != null)
    assertTrue(ser.intp == 123)
    assertTrue(ser.stringp == "abc")
    assertTrue(ser.listp.isInstanceOf[java.util.ArrayList[String]])
  }

  @Test
  def testInjectionWithInjectAnnotation() {
    class SimpleModule extends Config {
      bind [MyService, MyServiceImpl]
      bind [ClientWithAnnotation]
    }
    val m = new SimpleModule().create()
    val i = Guice.createInjector(m)
    
    val client = i.getInstance(Key.get(classOf[ClientWithAnnotation]))
    assertTrue(client.getMyService != null)
  }

  @Test
  def testInjectionWithBlock() {
    class SimpleModule extends Config {
      bind [MyService, MyServiceImpl]
      bind [ClientWithoutAnnotation] spec {
        property("myService")
      }
    }
    val m = new SimpleModule().create()
    val i = Guice.createInjector(m)
    
    val client = i.getInstance(Key.get(classOf[ClientWithoutAnnotation]))
    assertTrue(client.getMyService != null)
  }
  
  @Test
  def testInjectionWithBlockKnownName() {
    class SimpleModule extends Config {
      "service1" :: bind [MyServiceImpl]
      bind [ClientWithoutAnnotation] spec {
        property("myService") = ref("service1")
      }
    }
    val i = InjectorFactory.createInjector(new SimpleModule())

    val client = i.getInstance(Key.get(classOf[ClientWithoutAnnotation]))
    assertTrue(client.getMyService != null)
    assertTrue(client.getMyService.isInstanceOf[MyServiceImpl])
  }

  @Test
  def testInitCallback() {
    class SimpleModule extends Config {
      bind [MyServiceImpl] spec {
        lifecycle("init")
      }
    }
    val i = InjectorFactory.createInjector(new SimpleModule())

    val myi = i.getInstance(Key.get(classOf[MyServiceImpl]))
    assertTrue(myi.initCalled == true)
  }

  @Test
  def testInitWrongNameCallback() {
    class SimpleModule extends Config {
      bind [MyServiceImpl] spec {
        lifecycle("init_does_not_exist")
      }
    }
    try {
      InjectorFactory.createInjector(new SimpleModule())
      fail("Module creation should have thrown an exception")
    }
    catch {
      case e: Exception =>
    }
  }
  */

  @Test
  def testNestedBindings() {
    class SimpleModule extends Config {
      bind [NestedA, NestedAImpl1]
      bind [NestedB] spec {
        property("nestedA1")
        property("nestedA2") = bind [NestedAImpl2] spec {
          property("value") = "the_value"
        }
      }
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    val na1 = i.getInstance(Key.get(classOf[NestedA]))
    val na2 = i.getInstance(Key.get(classOf[NestedAImpl2]))
    val nb = i.getInstance(Key.get(classOf[NestedB]))

    assert(nb.nestedA1 == na1)
    assert(nb.nestedA2 == na2)
    assert(na2.value == "the_value")
  }

}
