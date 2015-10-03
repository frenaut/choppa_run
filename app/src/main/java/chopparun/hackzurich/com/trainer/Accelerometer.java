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
    private Sensor acceleration_;
    private Sensor rotation_;
    private boolean rotation_ready_ = false;

    //----------------------------------------------------------------------------------------------

    // Accelerometer
    float ax;
    float ay;
    float az;

    // Rotation
    float[] R = new float[9];

    //----------------------------------------------------------------------------------------------

    private RunTrackerService ctx;

    public Accelerometer(RunTrackerService context) {
        ctx = context;
        sensor_manager_ = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

        rotation_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensor_manager_.registerListener(this, rotation_, SensorManager.SENSOR_DELAY_FASTEST);

        // Gravity removed
        acceleration_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor == rotation_) {
            // R: World to Device
            SensorManager.getRotationMatrixFromVector(R, event.values);

            if (!rotation_ready_) {
                sensor_manager_.registerListener(this, acceleration_, SensorManager.SENSOR_DELAY_FASTEST);
                rotation_ready_ = true;
            }
        } else if (event.sensor == acceleration_) {
            // The acc sensor returns 3 values
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            //Log.d(TAG, "Acc: " + String.valueOf(ax) + ", " + String.valueOf(ay) + ", " + String.valueOf(az));

            // Multiply acceleration by R.t
            float nax = R[0]*ax + R[3]*ay + R[6]*az,
                  nay = R[1]*ax + R[4]*ay + R[7]*az,
                  naz = R[2]*ax + R[5]*ay + R[8]*az;

            ax = nax; ay = nay; az = naz;
            //Log.d(TAG, "Fixed acc: " + String.valueOf(nax) + ", " + String.valueOf(nay) + ", " + String.valueOf(naz));

            /*
            // Find R
            //     from      [gx, gy, gz]  to   [0, 0, -1]
            // Let  a = unit([gx, gy, gz]), b = [0, 0, -1]
            // NOTE: [gx, gy, gz] has to be unit vector
            // Then v = [-gy, -gx, 0], s = Math.sqrt(gx*gx + gy*gy), k = (1.0f-gz)/(gx2+gy2), c = gz
            //    v_x   = [0, 0, -gx; 0, 0, gy; gx, -gy, 0]
            //    v_x^2 = [gx^2, -gxgy, 0; -gxgy, gy^2, 0; 0, 0, gx^2+gy^2]
            //    k     = (1 - gz) / (gx*gx + gy*gy)
            // Then... R = [ 1 + k gx^2,     k gxgy,             -gx;
            //                   k gxgy, 1 + k gy^2,              gy;
            //                       gx,        -gy, 1 +k(gx^2-gy^2)]
            // Ref. http://math.stackexchange.com/questions/180418/calculate-rotation-matrix-to-align-vector-a-to-vector-b-in-3d
            float G = (float)Math.sqrt(gx*gx + gy*gy + gz*gz),
                  ngx = gx / G, ngy = gy / G, ngz = gz / G;
            float ngx2 = ngx*ngx, ngy2 = ngy*ngy, k = (1.0f-ngz)/(ngx2+ngy2);
            float kgx2 = k*ngx2;
            float R11 =  1 + kgx2, R12 =  k*ngx*ngy, R13 = -ngx,
                  R21 =       R12, R22 = 1 + k*ngy2, R23 =  ngy,
                  R31 =       ngx, R32 =       -ngy, R33 = 1 + k*(ngx2-ngy2);

            float n_ax = R11*ax + R12*ay + R13*az,
                  n_ay = R21*ax + R22*ay + R23*az,
                  n_az = R31*ax + R32*ay + R33*az;
            //Log.i(TAG, "norm g: " + String.valueOf(ngx) + ", " + String.valueOf(ngy) + ", " + String.valueOf(ngz));
            Log.i(TAG, "Fixed acc: " + String.valueOf(n_ax) + ", " + String.valueOf(n_ay) + ", " + String.valueOf(n_az));
            */

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
        float A = (float)Math.sqrt(ax*ax + ay*ay);
        //Log.d(TAG, "Acc: " + String.valueOf(A));

        float min_ = 0.5f;
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

