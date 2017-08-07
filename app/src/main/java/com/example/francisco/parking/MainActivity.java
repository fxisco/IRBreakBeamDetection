package com.example.francisco.parking;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class MainActivity extends Activity {

    static final String TAG = MainActivity.class.getSimpleName();
    static final String GPIO_NAME = "BCM4";
    Gpio mGpio;
    DatabaseReference mFirebaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mFirebaseReference = database.getReference("parking")
                .child("0")
                .child("0")
                .child("status");

        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            mGpio = manager.openGpio(GPIO_NAME);

            mGpio.setDirection(Gpio.DIRECTION_IN);
            mGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mGpio.setActiveType(Gpio.ACTIVE_HIGH);
        } catch (IOException e) {
            Log.e(TAG, "Unable to access GPIO.", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            mGpio.registerGpioCallback(mGpioCallback);
        } catch (IOException e) {
            Log.e(TAG, "Unable to register GPIO callback.", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGpio.unregisterGpioCallback(mGpioCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGpio != null) {
            try {
                mGpio.close();
                mGpio = null;
            } catch (IOException e) {
                Log.e(TAG, "Unable to close GPIO.", e);
            }
        }
    }

    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                mFirebaseReference.setValue(gpio.getValue());

                Log.i(TAG, "Setting value to: " + gpio.getValue());
            } catch (IOException e) {
                Log.i(TAG, "Unable to detect GPIO edge changes.");
            }

            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.e(TAG, gpio + ": Error event " + error);
        }
    };
}
