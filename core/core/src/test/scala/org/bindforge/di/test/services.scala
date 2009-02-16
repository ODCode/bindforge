package org.bindforge.di.test


trait MyService {
  def hello()
}

class MyServiceImpl extends MyService {

  var initCalled = false

  def init() {
    initCalled = true
  }

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

class ServiceWithIntProperty {

  var intProperty: Int = _

  def setIntProperty(i: Int) = intProperty = i
  def getIntProperty() = intProperty
}



