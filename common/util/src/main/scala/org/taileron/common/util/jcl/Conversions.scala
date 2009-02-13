/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/

package org.taileron.common.util.jcl

import java.util.{Dictionary, Enumeration}
import scala.Iterator
import scala.collection.Map

/**
 * Some implicit conversions from Scala to Java Collection types.
 */
object Conversions {

  /**
   * Implicitly converts from Scala Iterator to Java Enumeration.
   */
  implicit def iteratorToJavaEnumeration[T](iterator: Iterator[T]): Enumeration[T] = 
    iterator match {
      case null => null
      case _    => new Enumeration[T] {
        override def hasMoreElements = iterator.hasNext
        override def nextElement = iterator.next
      }
    }

  /**
   * Implicitly converts from Scala Iterator to Java Iterator. Read-only!
   */
  implicit def iteratorToJavaIterator[T](iterator: Iterator[T]): java.util.Iterator[T] =
    iterator match {
      case null => null
      case _    => new java.util.Iterator[T] {
        override def hasNext = iterator.hasNext
        override def next = iterator.next
        override def remove = throw new UnsupportedOperationException
      }
    }

  /**
   * Implicitly converts from Scala Map to Java Dictionary. Read-only!
   */
  implicit def mapToJavaDictionary[K, V](map: Map[K, V]): Dictionary[K, V] =
    if (map == null) null
    else new Dictionary[K, V] {
      override def size = map.size
      override def isEmpty = map.isEmpty
      override def keys = map.keys
      override def elements = map.values
      override def get(o: Object) = map.get(o.asInstanceOf[K]) match {
        case None => null.asInstanceOf[V]
        case Some(value: Any) => value.asInstanceOf[V]
      }
      override def put(key: K, value: V) = throw new UnsupportedOperationException
      override def remove(o: Object) = throw new UnsupportedOperationException
  }
}
