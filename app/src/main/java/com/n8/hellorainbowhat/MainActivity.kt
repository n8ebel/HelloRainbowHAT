package com.n8.hellorainbowhat

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import java.io.IOException


private const val BLINK_DURATION_MILLIS = 1000L

class MainActivity : Activity() {

    private val TAG = MainActivity::class.java.simpleName

    private val blinkHandler = Handler()
    private var redLED: Gpio? = null

    private val blinkRunnable = object : Runnable {
        override fun run() {
            redLED?.also {
                runSafeIO {
                    it.value = !it.value  // Update the GPIO state

                    // reschedule the update
                    blinkHandler.postDelayed(this, BLINK_DURATION_MILLIS)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runSafeIO {
            redLED = RainbowHat.openLedRed()

            blinkHandler.postDelayed(blinkRunnable, BLINK_DURATION_MILLIS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Remove pending blink Runnable from the handler.
        blinkHandler.removeCallbacks(blinkRunnable)

        runSafeIO {  redLED?.close() }
    }

    /**
     * Run the passed function in try/catch protecting against [IOException] that are
     * thrown by the RainbowHAT driver methods
     */
    private fun runSafeIO(ioOperation:() -> Unit) {
        try {
            ioOperation()
        } catch (error: IOException) {
            Log.e(TAG, "IO Error", error)
        }
    }
}
