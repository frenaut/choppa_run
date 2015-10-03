package chopparun.hackzurich.com.trainer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class Accelerometer implements SensorEventListener {
    private SensorManager sensor_manager_;
    private Sensor accelerometer_;

    private Context ctx;
    public Accelerometer(Context context) {
        ctx = context;
        sensor_manager_ = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accelerometer_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor_manager_.registerListener(this, accelerometer_, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The acc sensor returns 3 values
        float acc_x = event.values[0];
        float acc_y = event.values[1];
        float acc_z = event.values[2];
        // Do something with this sensor value.
    }

    protected void onDestroy() {

    }
}

