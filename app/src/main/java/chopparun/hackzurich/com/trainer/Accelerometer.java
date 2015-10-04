package chopparun.hackzurich.com.trainer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;

public class Accelerometer implements SensorEventListener {
    private final String TAG = "Accelerometer";

    //----------------------------------------------------------------------------------------------

    private SensorManager sensor_manager_;
    private Sensor acceleration_;
    private Sensor gravity_;
    private boolean gravity_ready_ = false;

    //----------------------------------------------------------------------------------------------

    // Accelerometer
    private float ax, ay, az;
    private float px, py, pz;
    private float nx, ny, nz;

    // gravity
    private float gx, gy, gz;

    //----------------------------------------------------------------------------------------------

    private RunTrackerService ctx;

    public Accelerometer(RunTrackerService context) {
        ctx = context;
        sensor_manager_ = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

        gravity_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensor_manager_.registerListener(this, gravity_, SensorManager.SENSOR_DELAY_FASTEST);

        // Gravity removed
        acceleration_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //sensor_manager_.registerListener(this, acceleration_, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor == gravity_) {
            final float alpha = 0.8f;
            gx = alpha * gx + (1 - alpha) * event.values[0];
            gy = alpha * gy + (1 - alpha) * event.values[1];
            gz = alpha * gz + (1 - alpha) * event.values[2];

            if (!gravity_ready_) {
                sensor_manager_.registerListener(this, acceleration_, SensorManager.SENSOR_DELAY_FASTEST);
                gravity_ready_ = true;
            }
        } else if (event.sensor == acceleration_) {
            px = ax; py = ay; pz = az;
            ax = nx; ay = ny; az = nz;

            // The acc sensor returns 3 values
            nx = event.values[0] - gx;
            ny = event.values[1] - gy;
            nz = event.values[2] - gz;

            // Try to smooth
            ax = 0.25f * px + 0.5f * ax + 0.25f * nx;
            ay = 0.25f * py + 0.5f * ay + 0.25f * ny;
            az = 0.25f * pz + 0.5f * az + 0.25f * nz;
            //Log.d(TAG, "Acc: " + String.valueOf(ax) + ", " + String.valueOf(ay) + ", " + String.valueOf(az));

            // Do something with this sensor value.
            detectStep();
        }
    }

    private float A_6 = 0, A_5 = 0, A_4 = 0, A_3 = 0, A_2 = 0, A_1 = 0; // magnitude of acc 4,3,2,1 reading prior

    private int total_steps = 0;
    private float prev_max = 0.0f;
    private long last_step_time_ = 0;
    private float prev_A = 0.0f;
    private boolean waiting_ = false;

    private void detectStep() {
        float A = (float)Math.sqrt(ax*ax + ay*ay + az*az);
        /*long current_time = new Date().getTime();
        if (A < 11.0f) return;
        else {
            if (!waiting_ && A < prev_A) {
                // Found max
                // Count step and wait
                total_steps++;
                last_step_time_ = current_time;
                ctx.onStepCount(total_steps);

                waiting_ = true;
            }
            else if (current_time - last_step_time_ > 50 && waiting_) {
                waiting_ = false;
            }
        }
        prev_A = A;*/
        //Log.d(TAG, "Acc: " + new Date().getTime() + " " + String.valueOf(A));

        if (A_3 > A_6 && A_3 > A_5 && A_3 > A_4 &&
            A_3 > A_2 && A_3 > A_1 && A_3 > A) {
            // Found maximum, now found distance from smallest to maximum
            float min_A = Math.min(A_6, Math.min(A_5, Math.min(A_4, Math.min(A_2, Math.min(A_1, A))))),
                  dA = A_3 - min_A;

            // Calculate threshold
            float min_dA = A_3 * 0.2f;
            if (A > 5.0f && min_dA > 0.5f && dA > min_dA) {
                total_steps += 1;
                ctx.onStepCount(total_steps);
            }
        }
        A_6 = A_5; A_5 = A_4; A_4 = A_3; A_3 = A_2; A_2 = A_1; A_1 = A;
    }

    protected void onDestroy() {
        sensor_manager_.unregisterListener(this);
    }
}
