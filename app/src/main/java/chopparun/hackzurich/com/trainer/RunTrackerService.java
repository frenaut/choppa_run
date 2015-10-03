package chopparun.hackzurich.com.trainer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.Date;
import java.util.ArrayList;


/*
    RunTrackerService is started and stopped by RunningActivity
 */
public class RunTrackerService extends Service implements SensorEventListener {
    // TAG for use in logging
    private static final String TAG = "RunTrackerService";

    //----------------------------------------------------------------------------------------------

    /* Audio related */
    private String coach_ = "Arnie"; // Current selected coach

    /* Steps counting related */
    ArrayList<Integer> steps_ ;// All steps accumulated. New cumulative step counts are appended.
    private long start_time_;  // Timestamp for value at steps_[0] (in ms, new Date().getTime())
    private long dtime_ = 500; // ms between each entry in steps_[]

    /*
        Personalization related
        - Take onCreate from RunningActivity (input in GoalEntry)
     */
    private int target_time_ = 600000; // in ms     (TODO: retrieve from activity)
    private int target_dist_ = 1000;   // in meters (TODO: retrieve from activity)

    //----------------------------------------------------------------------------------------------

    SensorManager sensor_manager_;
    Sensor step_counter_;

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    public void onSensorChanged(SensorEvent event) {
        int new_step_count = (int)event.values[0];
        onStepCount(new_step_count);
    }

    //----------------------------------------------------------------------------------------------

    private final IBinder binder_ = new LocalBinder();

    // Called when Activity (client) binds to service. Allows communication.
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");
        return binder_;
    }

    public class LocalBinder extends Binder {
        RunTrackerService getService() {
            // Allows client to call public methods in RunTrackerService
            return RunTrackerService.this;
        }
    }

    //----------------------------------------------------------------------------------------------

    // Called whenever activity requests the service to be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Tell activity about state
        Log.d(TAG, "onStartCommand called");

        return START_NOT_STICKY; // If killed, system does not restart the service
    }

    // Only called when service is started
    @Override
    public void onCreate() {
        sensor_manager_ = (SensorManager)getSystemService(SENSOR_SERVICE);
        step_counter_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensor_manager_.registerListener(this, step_counter_, SensorManager.SENSOR_DELAY_FASTEST);

        // TODO: Make run in foreground

        steps_= new ArrayList<>();
        start_time_ = new Date().getTime();

        // TODO: Start audio manager?
    }

    // Main pipeline
    public void onStepCount(int new_step_count) {
        Log.d(TAG, "new_step_count = " + String.valueOf(new_step_count));

        long current_time = new Date().getTime();
        String category;

        updateSteps(current_time, new_step_count);

        category = decide_category(current_time);

        if (!category.isEmpty()) play_audio(category);
    }

    // Update steps_ list
    private void updateSteps(long current_time, int new_step_count) {
        // Calculate index for current_time
        int index = (int)((current_time - start_time_)/dtime_); // this is ugly...

        // Fill empty time slots in steps_
        int last_step_count = steps_.size() == 0 ? new_step_count : steps_.get(steps_.size()-1);
        for (int i=0;i<index;i++) {
            steps_.add(i,last_step_count);
        }
        // Append new cumulative step count to steps_ with condition for empty steps
        steps_.add(index,new_step_count);
    }

    // play_audio plays a random audio file from a category
    private void play_audio(String category) {
        // TODO: Pick audio directory using `coach` and category
        // TODO: Choose random audio file from directory (For now just have a 01.mp3?)
    }

    // decide_category uses steps data collected so far to pick a audio category
    private String decide_category(long current_time) {
        //  Calculate metrics
        //       - distance left (target_dist - k * steps so far)
        int new_step_count = 0;
        if (steps_.size()!=0) {
            new_step_count = steps_.get(steps_.size()-1);
        }
        int left_dist = target_dist_-new_step_count;

        //       - current velocity (over past 10s)
        double vel = 0;
        if (steps_.size()>21)   {
            // at 500ms per entry, 10s corresponds to 20 entries ago
            vel = new_step_count - steps_.get(steps_.size()-21);
            vel = vel/10; // in m/s
        }
        //       - current acceleration (over past 10s) - Need to store velocities
        //       - time left
        int elapsed_time = (int)(current_time-start_time_);
        int left_time = target_time_-elapsed_time;
        //       - avg. velocity needed to reach goal
        double target_vel = left_dist / (left_time*1e3); // in m/s


        // Normalize metrics by target, in percent (acc does not need to be normalized)
        int vel_normalized = (int)(100*vel/target_vel);
        int time_normalized = (int)(100*(elapsed_time)/(target_time_));

        // Associate metrics with category
        // TODO : more categories
        String category="all_good";
        if (vel_normalized < 5) category="stopping";
        if (vel_normalized > 120) category="too_fast";
        if (vel_normalized < 80) category ="too_slow";

        Log.d(TAG, "Category picked: " + category);

        return category; // TODO: remove
    }
}
