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

package org.taileron.di.internal.compiler

import java.io.PrintWriter
import java.io.StringWriter
import java.util.LinkedList

import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.AbstractReporter
import scala.tools.nsc.util.Position


class IteratorWrapper[A](iter:java.util.Iterator[A]) {
  def foreach(f: A => Unit): Unit = {
    while(iter.hasNext){
      f(iter.next)
    }
  }
}

object BacklogReporter {
  val DEFAULT_SIZE = 50
}

class BacklogReporter(val settings: Settings, size: int) extends AbstractReporter {

  implicit def iteratorToWrapper[T](iter:java.util.Iterator[T]):IteratorWrapper[T] = new IteratorWrapper[T](iter)


  private val backLog = new LinkedList[Info]()

  def this(_settings: Settings) {
    this(_settings, BacklogReporter.DEFAULT_SIZE)
  }

  override def reset() {
    super.reset
    backLog.clear()
  }

  def display(pos: Position, msg: String, severity: Severity) {
    severity.count = severity.count + 1
    if (size > 0) {
      backLog.add(new Info(pos, msg, severity))
      if (backLog.size() > size) {
        backLog.remove(0);
      }
    }
  }

  def displayPrompt() {
  }

  override def toString(): String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    for (info <- backLog.iterator) {
      pw.println(info.toString)
    }
    sw.toString()
  }

  private class Info(pos: Position, msg: String, severity: Severity) {

    override def toString(): String = {
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      print(pw, severity)
      pw.print(" ")
      print(pw, pos)
      pw.print(": ")
      pw.print(msg)
      sw.toString()
    }

    private def print(pw: PrintWriter, pos: Position) {
      if (pos.source.isDefined) {
        pw.print(pos.source.get())
        pw.print(" ")
      }
      if (pos.line.isDefined) {
        pw.print("line ")
        pw.print(pos.line.get())
      }
    }

    private def print(pw: PrintWriter, severity: Severity) {
      if (INFO.equals(severity)) {
        pw.print("INFO")
      }
      else if (WARNING.equals(severity)) {
        pw.print("WARNING")
      }
      else if (ERROR.equals(severity)) {
        pw.print("ERROR")
      }
      else {
        throw new IllegalArgumentException("Severtiy out of range")
      }
    }

  }


}
