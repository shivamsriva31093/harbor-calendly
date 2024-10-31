package io.harbor.calendly.repositories

import io.harbor.calendly.util.ServiceCoroutineScope
import io.vertx.core.Vertx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseService(private val vertx: Vertx, serviceName: String) {
   protected val scope = ServiceCoroutineScope(vertx, serviceName)

   protected fun launch(block: suspend CoroutineScope.() -> Unit) {
       scope.launch { block() }
   }
}
