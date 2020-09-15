package helper

import com.github.shynixn.petblocks.api.business.service.LoggingService

class MockedLoggingService : LoggingService {
    /**
     * Logs an info text.
     */
    override fun info(text: String, e: Throwable?) {
        println(text)
        e?.printStackTrace()
    }

    /**
     * Logs an warning text.
     */
    override fun warn(text: String, e: Throwable?) {
        println(text)
        e?.printStackTrace()
    }

    /**
     * Logs an error text.
     */
    override fun error(text: String, e: Throwable?) {
        println(text)
        e?.printStackTrace()
    }
}