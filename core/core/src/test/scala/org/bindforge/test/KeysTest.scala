/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bindforge.test

import com.google.inject._
import com.google.inject.name._
import org.junit.Test
import org.junit.Assert._
import org.osgi.service.packageadmin.PackageAdmin


class KeysTest {

  @Test
  def testOneBindingNoId() {
    class SimpleModule extends Config {
      bind [MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    val bs = i.findBindingsByType(TypeLiteral.get(classOf[MyServiceImpl]))

    assertTrue(bs.size == 1)
    assertTrue(bs.get(0).getKey.getAnnotation == null)
  }

  @Test
  def testTwoBindingsNoId() {
    class SimpleModule extends Config {
      bind [MyServiceImpl]
      bind [MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    val bs = i.findBindingsByType(TypeLiteral.get(classOf[MyServiceImpl]))

    assertTrue(bs.size == 2)
    assertTrue(bs.get(0).getKey.getAnnotation != null)
    assertTrue(bs.get(1).getKey.getAnnotation != null)
  }

  @Test
  def testOneBindingOneId() {
    class SimpleModule extends Config {
      "service1" :: bind [MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())

    val bType = i.findBindingsByType(TypeLiteral.get(classOf[MyServiceImpl]))
    assertTrue(bType.size == 1)
    assertTrue(bType.get(0).getKey.getAnnotation == null)

    val bId = i.findBindingsByType(TypeLiteral.get(classOf[Object]))
    assertTrue(bId.size == 1)
    assertTrue(bId.get(0).getKey.getAnnotation != null)
  }

  @Test
  def testTwoBindingsOneId() {
    class SimpleModule extends Config {
      "service1" :: bind [MyServiceImpl]
      bind [MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())

    val bs = i.findBindingsByType(TypeLiteral.get(classOf[MyServiceImpl]))
    assertTrue(bs.size == 1)
    assertTrue(bs.get(0).getKey.getAnnotation == null)

    val bo = i.findBindingsByType(TypeLiteral.get(classOf[Object]))
    assertTrue(bo.size == 1)
    assertTrue(bo.get(0).getKey.getAnnotation != null)

    assertTrue(bs.get(0) != bo.get(0))
  }

  @Test
  def testTwoBindingsTwoIds() {
    class SimpleModule extends Config {
      "service1" :: bind [MyServiceImpl]
      "service2" :: bind [MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    val bo = i.findBindingsByType(TypeLiteral.get(classOf[Object]))
    assertTrue(bo.size == 2)
    assertTrue(bo.get(0).getKey.getAnnotation != null)
    assertTrue(bo.get(1).getKey.getAnnotation != null)
  }

  @Test
  def testOneServiceBindingNoId() {
    class SimpleModule extends Config {
      bind [MyService, MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    val bs = i.findBindingsByType(TypeLiteral.get(classOf[MyService]))
    assertTrue(bs.size == 1)
    assertTrue(bs.get(0).getKey.getAnnotation == null)

    val bImpl = i.findBindingsByType(TypeLiteral.get(classOf[MyServiceImpl]))
    assertTrue(bImpl.size == 0)
  }

  @Test
  def testOneServiceBindingAndOneImplBindingNoIds() {
    class SimpleModule extends Config {
      bind [MyService, MyServiceImpl]
      bind [MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    val bs = i.findBindingsByType(TypeLiteral.get(classOf[MyService]))
    assertTrue(bs.size == 1)
    assertTrue(bs.get(0).getKey.getAnnotation == null)

    val bImpl = i.findBindingsByType(TypeLiteral.get(classOf[MyServiceImpl]))
    assertTrue(bImpl.size == 1)
    assertTrue(bImpl.get(0).getKey.getAnnotation == null)

    assertTrue(bs.get(0) != bImpl.get(0))
  }

  @Test
  def testTwoServiceBindingNoIds() {
    class SimpleModule extends Config {
      bind [MyService, MyServiceImpl]
      bind [MyService, MyServiceImpl]
    }
    val i = InjectorFactory.createInjector(new SimpleModule())
    val bs = i.findBindingsByType(TypeLiteral.get(classOf[MyService]))
    assertTrue(bs.size == 2)
    assertTrue(bs.get(0).getKey.getAnnotation != null)
    assertTrue(bs.get(1).getKey.getAnnotation != null)
    assertTrue(bs.get(0) != bs.get(1))
  }

}
