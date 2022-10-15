package com.zionhuang.music.utils

import androidx.collection.LruCache
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MILLISECONDS

object InfoCache {
    private const val MAX_ITEMS_ON_CACHE = 60

    /**
     * Trim the cache to this size.
     */
    private const val TRIM_CACHE_TO = 30

    private val LRU_CACHE = LruCache<String, CacheData>(MAX_ITEMS_ON_CACHE)

    private fun keyOf(url: String): String = url

    private fun removeStaleCache() {
        LRU_CACHE.snapshot().forEach { (key, data) ->
            if (data != null && data.isExpired) {
                LRU_CACHE.remove(key)
            }
        }
    }

    fun getInfo(key: String): Any? {
        val data = LRU_CACHE[key] ?: return null
        if (data.isExpired) {
            LRU_CACHE.remove(key)
            return null
        }
        return data.info
    }

    private fun getFromKey(id: String): Any? = synchronized(LRU_CACHE) {
        getInfo(keyOf(id))
    }

    fun putInfo(id: String, info: Any, expirationMillis: Long = MILLISECONDS.convert(1, HOURS)) {
        synchronized(LRU_CACHE) {
            val data = CacheData(info, expirationMillis)
            LRU_CACHE.put(keyOf(id), data)
        }
    }

    fun removeInfo(id: String) {
        synchronized(LRU_CACHE) { LRU_CACHE.remove(keyOf(id)) }
    }

    fun clearCache() = synchronized(LRU_CACHE) {
        LRU_CACHE.evictAll()
    }

    fun trimCache() = synchronized(LRU_CACHE) {
        removeStaleCache()
        LRU_CACHE.trimToSize(TRIM_CACHE_TO)
    }

    val size: Int
        get() = synchronized(LRU_CACHE) {
            LRU_CACHE.size()
        }

    suspend fun <T : Any> checkCache(id: String, forceReload: Boolean = false, loadFromNetwork: suspend () -> T): T =
        if (!forceReload) {
            loadFromCache<T>(id)
        } else {
            null
        } ?: withContext(IO) {
            loadFromNetwork().also {
                putInfo(id, it)
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> loadFromCache(id: String): T? = getFromKey(id) as? T

    private class CacheData(val info: Any, timeoutMillis: Long) {
        private val expireTimestamp: Long = System.currentTimeMillis() + timeoutMillis
        val isExpired: Boolean get() = System.currentTimeMillis() > expireTimestamp
    }
}