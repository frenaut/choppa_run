package chopparun.hackzurich.com.trainer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

public class Accelerometer implements SensorEventListener {
    private final String TAG = "Accelerometer";

    //----------------------------------------------------------------------------------------------

    private SensorManager sensor_manager_;
    private Sensor accelerometer_;
    private Sensor gravity_;
    private boolean gravity_ready_ = false;

    //----------------------------------------------------------------------------------------------

    // Accelerometer
    float ax;
    float ay;
    float az;

    // Gravity
    float gx;
    float gy;
    float gz;

    //----------------------------------------------------------------------------------------------

    private RunTrackerService ctx;

    public Accelerometer(RunTrackerService context) {
        ctx = context;
        sensor_manager_ = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

        gravity_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensor_manager_.registerListener(this, gravity_, SensorManager.SENSOR_DELAY_GAME);

        accelerometer_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor == gravity_) {
            gx = event.values[0]; gy = event.values[1]; gz = event.values[2];
            //Log.d(TAG, "Gra: " + String.valueOf(gx) + ", " + String.valueOf(gy) + ", " + String.valueOf(gz));
            if (!gravity_ready_) {
                sensor_manager_.registerListener(this, accelerometer_, SensorManager.SENSOR_DELAY_GAME);
            }
        } else if (event.sensor == accelerometer_) {
            // The acc sensor returns 3 values
            ax = event.values[0] - gx;
            ay = event.values[1] - gy;
            az = event.values[2] - gz;
            //Log.d(TAG, "Acc: " + String.valueOf(ax) + ", " + String.valueOf(ay) + ", " + String.valueOf(az));

            // Do something with this sensor value.
            detectStep();
        }
    }

    private float A_4 = 0;
    private float A_3 = 0;
    private float A_2 = 0; // magnitude of acc 2 readings prior
    private float A_1 = 0; // magnitude of acc 1 reading prior
    private boolean step_state_ = false;
    private int total_steps = 0;

    private void detectStep() {
        float A = (float)Math.sqrt(ax*ax + ay*ay + az*az);
        //Log.d(TAG, "Acc: " + String.valueOf(A));

        float min_ = 1;
        if (A_2 > A_4 && A_2 > A_3 && A_2 > A_1 && A_2 > A &&
                (A_2 - A_4) > min_ && (A_2 - A) > min_) {
            if (step_state_) { // On step up
                total_steps += 1;
                ctx.onStepCount(total_steps);
            }
            step_state_ = !step_state_;
        }

        A_4 = A_3;
        A_3 = A_2;
        A_2 = A_1;
        A_1 = A;
    }

    protected void onDestroy() {
        sensor_manager_.unregisterListener(this);
    }
}

