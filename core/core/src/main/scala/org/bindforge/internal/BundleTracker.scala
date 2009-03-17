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

package org.bindforge.internal

import scala.collection.mutable.{HashSet, ListBuffer}

import org.osgi.framework._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ops4j.peaberry.Peaberry

import org.bindforge.common.util.jcl.Conversions._


class BundleTracker(context: BundleContext, bindforgeService: BindforgeService) extends BundleListener {
  
  private val logger = LoggerFactory.getLogger(this.getClass)

  private var active = false
  
  val BINDFORGE_DIR = "OSGI-INF/bindforge/"
  val BINDFORGE_HEADER = "BindForge-Config"
  
  val trackedBundles = new HashSet[Bundle]()

  val activeConfigs = new ListBuffer[Config]
  
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
      // Does the bundle has a config header?
      if (getConfigHeader(bundle) == null) {
        return false
      }
      // Else, track the bundle
      trackedBundles.addEntry(bundle)
      startTrackingBundle(bundle)
      true
    }
  }

  def startTrackingBundle(bundle: Bundle) {
    try {
      logger.info("Creating binding configuration for bundle [{}]", bundle.getSymbolicName)
      val scripts = bundle.findEntries(BINDFORGE_DIR, "*.scala", true).asInstanceOf[java.util.Enumeration[java.net.URL]]
      val bfb = new BindforgeBundle(context.getBundle, bundle, javaEnumerationToIterator(scripts))
      bfb.compile()
      val configClass = bfb.loadClass(getConfigHeader(bundle)).asInstanceOf[Class[Config]]
      val bindingConfig = configClass.newInstance
      val osgiModule = Peaberry.osgiModule(bundle.getBundleContext)
      InjectorFactory.createInjector(bindingConfig, osgiModule)
      
      activeConfigs += bindingConfig
    }
    finally {
      bindforgeService.addTrackedBundle(bundle.getBundleId)
    }
  }

  def stopTrackingBundle(bundle: Bundle) {
    logger.info("Closing binding configuration for bundle [{}]", bundle.getSymbolicName)
    bindforgeService.removeTrackedBundle(bundle.getBundleId)
    activeConfigs.foreach(_.shutdown())
  }

  private def getConfigHeader(bundle: Bundle): String = {
    bundle.getHeaders.get(BINDFORGE_HEADER).asInstanceOf[String]
  }

}
