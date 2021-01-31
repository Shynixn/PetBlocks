package helper

import com.github.shynixn.petblocks.api.business.service.CoroutineSessionService
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MockedCoroutineSessionService : CoroutineSessionService {
    /**
     * Launch.
     */
    override fun launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit): Job {
        return GlobalScope.launch(dispatcher, CoroutineStart.DEFAULT, f)
    }

    /**
     * Minecraft.
     */
    override val minecraftDispatcher: CoroutineContext
        get() = Dispatchers.Unconfined

    /**
     * Async.
     */
    override val asyncDispatcher: CoroutineContext
        get() = Dispatchers.IO

    /**
     * Scope.
     */
    override val scope: CoroutineScope
        get() = GlobalScope
}
