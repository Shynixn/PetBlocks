package com.github.shynixn.petblocks.api.persistence.context

import java.sql.Connection

interface SqlContext {
    /**
     * Gets a new connection to the database.
     * Caller is responsible for closing the connection after using.
     */
    fun getConnection(): Connection
}
