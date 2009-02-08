package org.scalamodules.di.internal

import scala.collection.mutable.HashSet

import org.osgi.framework._

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.inject.Guice

import org.scalamodules.di._


class BundleTracker(context: BundleContext) extends BundleListener {
  
  private val logger = LoggerFactory.getLogger(this.getClass)
  
  val DEFAULT_BINDING_CONFIG_DIR = "OSGI-INF/taileron/"
  val DEFAULT_BINDING_CONFIG_FILE = "binding.tsc"
  
  val trackedBundles = new HashSet[Bundle]()
  
  def start() {
    synchronized {
      context.addBundleListener(this)
      context.getBundles.foreach(analyseBundleForTracking)
    }
  }
  
  def bundleChanged(event: BundleEvent) {
    if (event.getType == BundleEvent.STOPPED && trackedBundles.contains(event.getBundle)) {
      trackedBundles.removeEntry(event.getBundle)
      stopTrackingBundle(event.getBundle)
    }
    else {
      analyseBundleForTracking(event.getBundle)
    }
  }

  def stop() {
    synchronized {
      context.removeBundleListener(this)
      trackedBundles.foreach(stopTrackingBundle)
    }
  }

  def analyseBundleForTracking(bundle: Bundle): Boolean = {
    synchronized {
      logger.debug("Analysing bundle [{}]", bundle.getSymbolicName)

      // Is bundle ACTIVE?
      if (bundle.getState != Bundle.ACTIVE) {
        logger.debug("Bundle [{}] not ACTIVE", bundle.getSymbolicName)
        return false
      }

      // Already tracked?
      if (trackedBundles.contains(bundle)) {
        logger.debug("Bundle [{}] already processed", bundle.getSymbolicName)
        return false
      }

      // If the bundle does not contain a binding config, return false
      val enums = bundle.findEntries(DEFAULT_BINDING_CONFIG_DIR, DEFAULT_BINDING_CONFIG_FILE, false)
      if (enums == null || !enums.hasMoreElements) {
        logger.debug("Bundle [{}] does not have binding configuration", bundle.getSymbolicName)
        return false
      }

      // Else, track the bundle
      trackedBundles.addEntry(bundle)
      startTrackingBundle(bundle)
      true
    }
  }

  def startTrackingBundle(bundle: Bundle) {
    logger.info("Creating binding configuration for bundle [{}]", bundle.getSymbolicName)
    val enums = bundle.findEntries(DEFAULT_BINDING_CONFIG_DIR, DEFAULT_BINDING_CONFIG_FILE, false)
    val url = enums.nextElement.asInstanceOf[java.net.URL]
    val cf = new ConfigFile(context, url)
    cf.compile()
  }

  def stopTrackingBundle(bundle: Bundle) {
    logger.info("Closing binding configuration for bundle [{}]", bundle.getSymbolicName)
  }

}
