/*
 * Bindable.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.scalamodules.di

import com.google.inject.Binder


trait Bindable[A <: Object] {

  def create(binder: Binder)

}
