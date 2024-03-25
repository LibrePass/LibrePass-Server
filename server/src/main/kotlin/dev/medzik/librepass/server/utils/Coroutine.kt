package dev.medzik.librepass.server.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object CustomDispatchers {
    /**
     * A custom dispatcher that uses a virtual thread.
     */
    var LOOM: CoroutineDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
}
