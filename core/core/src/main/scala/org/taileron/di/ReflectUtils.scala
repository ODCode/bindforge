/*
 * Copyright 2009 Roman Roelofsen
 *
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

package org.taileron.di

import java.lang.reflect.Method


object ReflectUtils {

  def getMethod(clazz: Class[_], name: String): Method = {
    val methods = clazz.getMethods.filter(_.getName == name)
    if (methods.size == 0) {
      return null
    }
    else {
      return methods(0)
    }
  }

}
