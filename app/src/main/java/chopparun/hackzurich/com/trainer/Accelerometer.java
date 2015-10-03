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
    private Sensor rotation_;
    private boolean rotation_ready_ = false;

    //----------------------------------------------------------------------------------------------

    // Accelerometer
    private float ax, ay, az;
    private float px, py, pz;
    private float nx, ny, nz;

    // Rotation
    float[] R = new float[9];

    //----------------------------------------------------------------------------------------------

    private RunTrackerService ctx;

    public Accelerometer(RunTrackerService context) {
        ctx = context;
        sensor_manager_ = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

        rotation_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //sensor_manager_.registerListener(this, rotation_, SensorManager.SENSOR_DELAY_FASTEST);

        // Gravity removed
        acceleration_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensor_manager_.registerListener(this, acceleration_, SensorManager.SENSOR_DELAY_NORMAL);
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
                sensor_manager_.registerListener(this, acceleration_, SensorManager.SENSOR_DELAY_GAME);
                rotation_ready_ = true;
            }
        } else if (event.sensor == acceleration_) {
            px = ax; py = ay; pz = az;
            ax = nx; ay = ny; az = nz;

            // The acc sensor returns 3 values
            nx = event.values[0];
            ny = event.values[1];
            nz = event.values[2];

            // Try to smooth
            ax = 0.3333f * (px + ax + nx);
            ay = 0.3333f * (py + ay + ny);
            az = 0.3333f * (pz + az + nz);
            //Log.d(TAG, "Acc: " + String.valueOf(ax) + ", " + String.valueOf(ay) + ", " + String.valueOf(az));

            /*
            // Multiply acceleration by R.t
            float nax = R[0]*ax + R[3]*ay + R[6]*az,
                  nay = R[1]*ax + R[4]*ay + R[7]*az,
                  naz = R[2]*ax + R[5]*ay + R[8]*az;

            ax = nax; ay = nay; az = naz;
            */
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

    private float A_4 = 0, A_3 = 0, A_2 = 0, A_1 = 0; // magnitude of acc 4,3,2,1 reading prior
    private long t_4, t_3, t_2, t_1; // timestamps of 4,3,2,1 reading prior
    {
        long t = new Date().getTime();
        t_4 = t_3 = t_2 = t_1 = t;
    }
    private float v_4, v_3, v_2, v_1;
    private float d_4, d_3, d_2, d_1;

    private int total_steps = 0;

    private void detectStep() {
        float A = (float)Math.sqrt(ax*ax + ay*ay + az*az);
        long t = new Date().getTime();
        //Log.d(TAG, "Acc: " + String.valueOf(A));

        float v = 0.5f * ((t_4 - t_3) * (A_4 + A_3) + (t_3 - t_2) * (A_3 + A_2) +
                  (t_2 - t_1) * (A_2 + A_1) + (t_1 - t) * (A_1 + A));
        v = Math.abs(v);
        float d = 0.5f * ((t_4 - t_3) * (v_4 + v_3) + (t_3 - t_2) * (v_3 + v_2) +
                (t_2 - t_1) * (v_2 + v_1) + (t_1 - t) * (v_1 + v));
        d = Math.abs(d);

        float min_ = 0.1f;
        if (A_2 > A_4 && A_2 > A_3 && A_2 > A_1 && A_2 > A && (A_2 - A_4) > min_ && (A_2 - A) > min_)
            Log.d(TAG, "Maximum: " + String.valueOf(A_2));
        if (A_2 < A_4 && A_2 < A_3 && A_2 < A_1 && A_2 < A && (A_4 - A_2) > min_ && (A - A_2) > min_)
            Log.d(TAG, "Minimum: " + String.valueOf(A_2));
        
        if (A_2 > A_4 && A_2 > A_3 && A_2 > A_1 && A_2 > A &&
                (A_2 - A_4) > min_ && (A_2 - A) > min_) {
            total_steps += 1;
            //Log.d(TAG, "v: " + String.valueOf(v) + ", d: " + String.valueOf(d));
            ctx.onStepCount(total_steps, d);
        }

        A_4 = A_3; A_3 = A_2; A_2 = A_1; A_1 = A;
        t_4 = t_3; t_3 = t_2; t_2 = t_1; t_1 = t;
        v_4 = v_3; v_3 = v_2; v_2 = v_1; v_1 = v;
        d_4 = d_3; d_3 = d_2; d_2 = d_1; d_1 = d;
    }

    protected void onDestroy() {
        sensor_manager_.unregisterListener(this);
    }
}
