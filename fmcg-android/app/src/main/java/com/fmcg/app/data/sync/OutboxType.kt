package com.fmcg.app.data.sync

enum class OutboxType { STORE_CREATE, STORE_UPDATE, STORE_DELETE, ORDER_CREATE }

/** Retry ceiling. A row that reaches this many attempts is parked (dead) with
 *  its lastError, and no longer retried. */
const val MAX_SYNC_ATTEMPTS = 5
