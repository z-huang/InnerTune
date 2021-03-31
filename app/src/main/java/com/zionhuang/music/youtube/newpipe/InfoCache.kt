package com.zionhuang.music.youtube.newpipe

import androidx.collection.LruCache
import org.schabi.newpipe.extractor.InfoItem.InfoType
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

    private fun getInfo(key: String): Any? {
        val data = LRU_CACHE[key] ?: return null
        if (data.isExpired) {
            LRU_CACHE.remove(key)
            return null
        }
        return data.info
    }

    fun getFromKey(url: String): Any? =
            synchronized(LRU_CACHE) { getInfo(keyOf(url)) }

    fun putInfo(url: String, info: Any) {
        val expirationMillis = MILLISECONDS.convert(1, HOURS)
        synchronized(LRU_CACHE) {
            val data = CacheData(info, expirationMillis)
            LRU_CACHE.put(keyOf(url), data)
        }
    }

    fun removeInfo(url: String, infoType: InfoType) {
        synchronized(LRU_CACHE) { LRU_CACHE.remove(keyOf(url)) }
    }

    fun clearCache() = synchronized(LRU_CACHE) { LRU_CACHE.evictAll() }

    fun trimCache() = synchronized(LRU_CACHE) {
        removeStaleCache()
        LRU_CACHE.trimToSize(TRIM_CACHE_TO)
    }

    val size: Int get() = synchronized(LRU_CACHE) { LRU_CACHE.size() }

    private class CacheData(val info: Any, timeoutMillis: Long) {
        private val expireTimestamp: Long = System.currentTimeMillis() + timeoutMillis
        val isExpired: Boolean get() = System.currentTimeMillis() > expireTimestamp
    }
}