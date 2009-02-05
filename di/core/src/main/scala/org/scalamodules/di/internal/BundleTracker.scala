package org.scalamodules.di.internal

import scala.collection.mutable.HashSet

import org.osgi.framework._

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.inject.Guice

import org.scalamodules.di._


class BundleTracker(context: BundleContext) extends BundleListener {
  
  private val logger = LoggerFactory.getLogger(this.getClass)
  
  val SCALAMODULES_CONFIG = "Binding-Config"
  
  val trackedBundles = new HashSet[Bundle]()
  
  def start() {
    synchronized {
      context.addBundleListener(this)
      context.getBundles.foreach(addBundle)
    }
  }
  
  def stop() {
    synchronized {
      trackedBundles.foreach(removeBundle)
      context.removeBundleListener(this)  
    }
  }
  
  def bundleChanged(event: BundleEvent) {
    if (event.getType == BundleEvent.STARTED)
      addBundle(event.getBundle)
    else if (event.getType == BundleEvent.STOPPED)
      removeBundle(event.getBundle)
  } 
  
  def addBundle(bundle: Bundle) {
    synchronized {
      val configHeader = bundle.getHeaders().get(SCALAMODULES_CONFIG)
      if (configHeader != null) {
        onStartedBundle(bundle, configHeader.asInstanceOf[String])
        trackedBundles.addEntry(bundle)
      }
    }
  }
  
  def removeBundle(bundle: Bundle) {
    synchronized {
      if (trackedBundles.contains(bundle))
        onStoppedBundle(bundle)
      trackedBundles.removeEntry(bundle)
    }
  }
  
  def onStartedBundle(bundle: Bundle, config: String) {
    println("Bundle with binding configuration STARTED [" + bundle + "]")
    val clazz = bundle.loadClass(config)
    val instance = clazz.newInstance().asInstanceOf[BindingConfig]
    val module = instance.create()
    
    val injector = Guice.createInjector(module)
    injector
  }
  
  def onStoppedBundle(bundle: Bundle) {
    println("Bundle with binding configuration STOPPED [" + bundle + "]")
  }
  
  
}
