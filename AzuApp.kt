package com.mahin.azu

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.mahin.azu.data.repository.AppRepository

/**
 * Application class for A-ZU.
 *
 * Low-RAM tuning for Android Go / Redmi A2+:
 * - Coil RGB_565 halves icon memory vs ARGB_8888.
 * - Memory cache capped at 15% (not the 25% default), disk cache 32 MB.
 * - No background services, no periodic work.
 */
class AzuApp : Application(), ImageLoaderFactory {

    lateinit var appRepository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        appRepository = AppRepository(applicationContext)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.15).build() }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_icons"))
                    .maxSizeBytes(32L * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(false)
            .build()
    }
}
