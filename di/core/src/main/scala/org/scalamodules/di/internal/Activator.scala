package org.scalamodules.di.internal

import org.osgi.framework._

class Activator extends BundleActivator {
  
  var bundleTracker: BundleTracker = _
  
  def start(context: BundleContext) {
    bundleTracker = new BundleTracker(context)
    bundleTracker.start()
  }

  def stop(context: BundleContext) {
    bundleTracker.stop()
  }
  
}


