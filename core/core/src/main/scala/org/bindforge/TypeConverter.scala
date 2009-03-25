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

package org.bindforge

object TypeConverter {

  val JavaList = classOf[java.util.List[_]]
  val JavaMap = classOf[java.util.Map[_, _]]

  def convert(targetType: Class[_], value: Any): Any = value match {
    // From...
    // List...
    case from: List[_] => targetType match {
        // ... to java.util.List
        case JavaList =>
          val javaList = new java.util.ArrayList[Any]
          from.foreach(javaList.add(_))
          javaList

        case _ => from
      }

    // Map...
    case from: Map[_, _] => targetType match {
        case JavaMap =>
          // todo
          
        case _ => from
      }
      
    case _ => value
  }

}
