package org.scalamodules.di.test


trait MyService {
  def hello()
}

class MyServiceImpl extends MyService {
  def hello() = println("Hello")
}

class ClientWithAnnotation {
  
  var myService: MyService = _
  
  @com.google.inject.Inject  
  def setMyService(myService: MyService) {
    this.myService = myService
  }
  
  def getMyService(): MyService = myService
}

class ClientWithoutAnnotation {
  
  var myService: MyService = _
  
  def setMyService(myService: MyService) {
    this.myService = myService
  }
  
  def getMyService(): MyService = myService
}
