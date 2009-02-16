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

package org.bindforge.di.internal

import scala.collection.mutable.HashSet

import org.osgi.framework._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.google.inject.Guice

import org.bindforge.di._


class BundleTracker(context: BundleContext, bindforgeService: BindforgeService) extends BundleListener {
  
  private val logger = LoggerFactory.getLogger(this.getClass)

  private var active = false
  
  val DEFAULT_BINDING_CONFIG_DIR = "OSGI-INF/bindforge/"
  val DEFAULT_BINDING_CONFIG_FILE = "module.scala"
  
  val trackedBundles = new HashSet[Bundle]()
  
  def start() {
    if (active) {
      logger.info("BundleTracker can not be started. Reason: Already active")
      return
    }
    synchronized {
      active = true
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
    if (!active) {
      logger.info("BundleTracker can not be stopped. Reason: Not active")
      return
    }
    synchronized {
      active = false
      context.removeBundleListener(this)
      trackedBundles.foreach(stopTrackingBundle)
    }
  }

  def analyseBundleForTracking(bundle: Bundle): Boolean = {
    synchronized {
      // Is bundle ACTIVE?
      if (bundle.getState != Bundle.ACTIVE) {
        return false
      }

      // Already tracked?
      if (trackedBundles.contains(bundle)) {
        return false
      }

      // If the bundle does not contain a binding config, return false
      val enums = bundle.findEntries(DEFAULT_BINDING_CONFIG_DIR, DEFAULT_BINDING_CONFIG_FILE, false)
      if (enums == null || !enums.hasMoreElements) {
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
    val cf = new ConfigFile(context.getBundle, bundle, url)
    cf.getBindingConfigClass()
    bindforgeService.addTrackedBundle(bundle.getBundleId)
  }

  def stopTrackingBundle(bundle: Bundle) {
    logger.info("Closing binding configuration for bundle [{}]", bundle.getSymbolicName)
    bindforgeService.removeTrackedBundle(bundle.getBundleId)
  }

}
