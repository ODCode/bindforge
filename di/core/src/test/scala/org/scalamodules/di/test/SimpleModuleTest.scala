package org.scalamodules.di.test

import com.google.inject._
import com.google.inject.name._
import org.junit.Test
import org.junit.Assert._
import org.osgi.service.packageadmin.PackageAdmin

import org.scalamodules.di._


class SimpleModuleTest {

  @Test
  def testSimpleBind() {
    class SimpleModule extends BindingConfig {
      bind[MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    
    val myi = i.getInstance(Key.get(classOf[MyServiceImpl]))
    assertTrue(myi != null)
  }

  
  @Test
  def testFromAndToType() {
    class SimpleModule extends BindingConfig {
      bind[MyService, MyServiceImpl]
    }
        
    val i = InjectorFactory.createInjector(new SimpleModule())
    
    val ser = i.getInstance(Key.get(classOf[MyService]))
    assertTrue(ser != null)
  }

  @Test
  def testId() {
    class SimpleModule extends BindingConfig {
      "s1" :: bind[MyServiceImpl]
    }
    val m = new SimpleModule().create()
    val i = Guice.createInjector(m)
    
    val ser = i.getInstance(Key.get(classOf[Object], Names.named("s1")))
    assertTrue(ser != null)
  }

  @Test
  def testInjectionWithInjectAnnotation() {
    class SimpleModule extends BindingConfig {
      bind[MyService, MyServiceImpl]
      bind[ClientWithAnnotation]
    }
    val m = new SimpleModule().create()
    val i = Guice.createInjector(m)
    
    val client = i.getInstance(Key.get(classOf[ClientWithAnnotation]))
    assertTrue(client.getMyService != null)
  }

  @Test
  def testInjectionWithBlock() {
    class SimpleModule extends BindingConfig {
      bind[MyService, MyServiceImpl]
      bind[ClientWithoutAnnotation] - {
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
    class SimpleModule extends BindingConfig {
      "service1" :: bind[MyServiceImpl]
      bind[ClientWithoutAnnotation] - {
        property("myService") <-- "service1"
      }
    }
    val i = InjectorFactory.createInjector(new SimpleModule())

    val client = i.getInstance(Key.get(classOf[ClientWithoutAnnotation]))
    assertTrue(client.getMyService != null)
    assertTrue(client.getMyService.isInstanceOf[MyServiceImpl])
  }


}
