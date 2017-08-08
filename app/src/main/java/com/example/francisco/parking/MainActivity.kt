package com.example.francisco.parking

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : Activity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val GPIO_NAME = "BCM4"
        private val PLACE_ID = "0"
        private val FIREBASE_NODE_PARKING = "parking"
        private val PARKING_LOT_STATUS_FIELD = "status"
    }

    private var mGpio: Gpio? = null
    private lateinit var mDatabaseReference: DatabaseReference

    private val mGpioCallback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio?): Boolean {
            executeInTry("Unable to detect GPIO edge changes.") {
                gpio?.let {
                    val value = it.value

                    mDatabaseReference.setValue(value)

                    Log.i(TAG, "Setting value to: $value")
                }
            }

            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            Log.e(TAG, "$gpio: error event $error")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference(FIREBASE_NODE_PARKING)
                .child(PLACE_ID)
                .child(PLACE_ID)
                .child(PARKING_LOT_STATUS_FIELD);

        executeInTry("Unable to access GPIO with name $GPIO_NAME") {
            mGpio = PeripheralManagerService().openGpio(GPIO_NAME).apply {
                setDirection(Gpio.DIRECTION_IN)
                setEdgeTriggerType(Gpio.EDGE_BOTH)
                setActiveType(Gpio.ACTIVE_HIGH)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        executeInTry("Unable to register GPIO callback.") {
            mGpio?.registerGpioCallback(mGpioCallback)
        }
    }

    override fun onStop() {
        super.onStop()

        mGpio?.unregisterGpioCallback(mGpioCallback)
    }

    override fun onDestroy() {
        super.onDestroy()

        executeInTry("Unable to close GPIO: $GPIO_NAME") {
            mGpio?.close()
            mGpio = null
        }
    }

    /**
     * Executes some code inside try-catch block.
     *
     * @param errorMessage The error message to print in case of an exception.
     * @param body The code block to be executed.
     */
    fun executeInTry(errorMessage: String, body: () -> Unit) {
        try {
            body()
        } catch (ex: Exception) {
            Log.e(TAG, errorMessage, ex)
        }
    }
}
