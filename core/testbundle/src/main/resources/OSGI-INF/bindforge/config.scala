/*
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

package org.bindforge.test.testbundle


class Config extends org.bindforge.Config {

  bind [ExportServiceWithProps, ExportServiceWithPropsImpl] spec {
    "exportServiceWithPropsHandle" :: exportService("key1" -> "value1", "key2" -> "value2")
  }

  bind [ExportServiceWithPropsClient] spec {
    property("selfExportHandle") = exportService
    property("exportServiceWithPropsHandle") = ref("exportServiceWithPropsHandle")
  }

  bind [ServiceWithConfig] spec {
    exportService
    config("servicewithconfig.pid", "updated")
  }

}

