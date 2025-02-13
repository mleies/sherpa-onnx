// Copyright (c)  2023  Xiaomi Corporation
package com.k2fsa.sherpa.onnx

import android.content.res.AssetManager

data class OfflineTtsVitsModelConfig(
    var model: String,
    var lexicon: String,
    var tokens: String,
    var noiseScale: Float = 0.667f,
    var noiseScaleW: Float = 0.8f,
    var lengthScale: Float = 1.0f,
)

data class OfflineTtsModelConfig(
    var vits: OfflineTtsVitsModelConfig,
    var numThreads: Int = 1,
    var debug: Boolean = false,
    var provider: String = "cpu",
)

data class OfflineTtsConfig(
    var model: OfflineTtsModelConfig,
)

class GeneratedAudio(
    val samples : FloatArray,
    val sampleRate: Int,
) {
    fun save(filename: String) = saveImpl(filename=filename, samples=samples, sampleRate=sampleRate)

    private external fun saveImpl(
        filename: String,
        samples: FloatArray,
        sampleRate: Int
    ): Boolean
}

class OfflineTts(
    assetManager: AssetManager? = null,
    var config: OfflineTtsConfig,
) {
    private var ptr: Long

    init {
        if (assetManager != null) {
            ptr = new(assetManager, config)
        } else {
            ptr = newFromFile(config)
        }
    }

    fun generate(
        text: String,
        sid: Int = 0,
        speed: Float = 1.0f
    ): GeneratedAudio {
      var objArray = generateImpl(ptr, text=text, sid=sid, speed=speed)
      return GeneratedAudio(samples=objArray[0] as FloatArray,
                            sampleRate=objArray[1] as Int)
    }

    fun allocate(assetManager: AssetManager? = null) {
      if (ptr == 0L) {
        if (assetManager != null) {
          ptr = new(assetManager, config)
        } else {
          ptr = newFromFile(config)
        }
      }
    }

    fun free() {
      if (ptr != 0L) {
        delete(ptr)
        ptr = 0
      }
    }

    protected fun finalize() {
        delete(ptr)
    }

    private external fun new(
        assetManager: AssetManager,
        config: OfflineTtsConfig,
    ): Long

    private external fun newFromFile(
        config: OfflineTtsConfig,
    ): Long

    private external fun delete(ptr: Long)

    // The returned array has two entries:
    //  - the first entry is an 1-D float array containing audio samples.
    //    Each sample is normalized to the range [-1, 1]
    //  - the second entry is the sample rate
    external fun generateImpl(
        ptr: Long,
        text: String,
        sid: Int = 0,
        speed: Float = 1.0f
    ): Array<Any>

    companion object {
        init {
            System.loadLibrary("sherpa-onnx-jni")
        }
    }

}
