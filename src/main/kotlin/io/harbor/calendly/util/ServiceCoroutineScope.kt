package io.harbor.calendly.util

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class ServiceCoroutineScope(
   vertx: Vertx,
   private val serviceName: String
) : CoroutineScope {
   override val coroutineContext: CoroutineContext = vertx.dispatcher() +
       CoroutineName(serviceName) +
       SupervisorJob() +
       CoroutineExceptionHandler { _, throwable ->
           println("Error in $serviceName: ${throwable.message}")
       }
}
