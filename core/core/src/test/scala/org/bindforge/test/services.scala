package org.bindforge.test


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
  
  var _myService: MyService = _
  
  def setMyService(myService: MyService) {
    this._myService = myService
  }
  
  def getMyService(): MyService = _myService
}

class ServiceWithProperties {

  var intp: Int = _
  var stringp: String = _
  
  var javaList: java.util.List[String] = _

}


trait NestedA {
  var value: String = ""
}
class NestedAImpl1 extends NestedA
class NestedAImpl2 extends NestedA

class NestedB {
  var nestedA1: NestedA = _
  var nestedA2: NestedA = _
}













