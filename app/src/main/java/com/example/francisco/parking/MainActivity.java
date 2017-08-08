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
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String GPIO_NAME = "BCM4";
    private static final String PLACE_ID = "0";
    private static final String FIREBASE_NODE_PARKING = "parking";
    private static final String PARKING_LOT_STATUS_FIELD = "status";
    private Gpio mGpio;
    private DatabaseReference mFirebaseReference;

    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                mFirebaseReference.setValue(gpio.getValue());

                Log.i(TAG, String.format("Setting value to: %s", gpio.getValue()));
            } catch (IOException e) {
                Log.i(TAG, "Unable to detect GPIO edge changes.");
            }

            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.e(TAG, String.format("%s: error event %s", gpio, error));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseReference = FirebaseDatabase.getInstance()
                .getReference(FIREBASE_NODE_PARKING)
                .child(PLACE_ID)
                .child(PLACE_ID)
                .child(PARKING_LOT_STATUS_FIELD);

        try {
            mGpio = new PeripheralManagerService()
                    .openGpio(GPIO_NAME);

            mGpio.setDirection(Gpio.DIRECTION_IN);
            mGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mGpio.setActiveType(Gpio.ACTIVE_HIGH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Unable to access GPIO with name: %s", GPIO_NAME), ex);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGpio != null) {
            try {
                mGpio.registerGpioCallback(mGpioCallback);
            } catch (IOException ex) {
                Log.e(TAG, "Unable to register GPIO callback.", ex);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGpio != null) {
            mGpio.unregisterGpioCallback(mGpioCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGpio != null) {
            try {
                mGpio.close();
                mGpio = null;
            } catch (IOException ex) {
                Log.e(TAG, String.format("Unable to close GPIO: %s", GPIO_NAME), ex);
            }
        }
    }
}
