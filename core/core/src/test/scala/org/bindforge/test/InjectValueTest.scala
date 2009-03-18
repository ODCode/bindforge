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


class InjectValueTest {

  @Test
  def testInjectValue() {
    class SimpleModule extends Config {
      bind [ServiceWithProperties] spec {
        property("intp") = 123
        property("stringp") = "abc"
      }
    }
    val i = InjectorFactory.createInjector(new SimpleModule)

    val ser = i.getInstance(Key.get(classOf[ServiceWithProperties]))
    assertTrue(ser != null)
    assertTrue(ser.intp == 123)
    assertTrue(ser.stringp == "abc")
  }

  @Test
  def testJavaList() {
    class SimpleModule extends Config {
      bind [ServiceWithProperties] spec {
        property("javaList") = List(1, 2, 3)
      }
    }
    val i = InjectorFactory.createInjector(new SimpleModule)

    val ser = i.getInstance(Key.get(classOf[ServiceWithProperties]))
    assertTrue(ser.javaList.isInstanceOf[java.util.ArrayList[_]])
    assertTrue(ser.javaList.get(0) == 1)
    assertTrue(ser.javaList.get(1) == 2)
    assertTrue(ser.javaList.get(2) == 3)
  }

}
