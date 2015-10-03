package chopparun.hackzurich.com.trainer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/*
    RunTrackerService is started and stopped by RunningActivity
 */
public class RunTrackerService extends Service implements SensorEventListener {
    // TAG for use in logging
    private static final String TAG = "RunTrackerService";

    //----------------------------------------------------------------------------------------------

    /* Audio related */
    private String coach_ = "arnie"; // Current selected coach

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

    private Timer timer_ = new Timer();

    // Only called when service is started
    @Override
    public void onCreate() {
        sensor_manager_ = (SensorManager)getSystemService(SENSOR_SERVICE);
        step_counter_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensor_manager_.registerListener(this, step_counter_, SensorManager.SENSOR_DELAY_FASTEST);

        // TODO: Make run in foreground

        steps_= new ArrayList<>();
        start_time_ = new Date().getTime();

        timer_.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                check();
            }
        }, new Date(), 500);
    }

    // Main pipeline
    public void onStepCount(int new_step_count) {
        Log.d(TAG, "new_step_count = " + String.valueOf(new_step_count));

        long current_time = new Date().getTime();
        addSteps(current_time, new_step_count);
    }

    public void check() {
        if (playing_) return;

        long current_time = new Date().getTime();
        updateSteps(current_time);
        String category = decide_category(current_time);
        play_audio(category);
    }

    // Update steps_ list
    private void updateSteps(long current_time) {
        // Calculate index for current_time
        int index = (int)((current_time - start_time_)/dtime_); // this is ugly...

        // Fill empty time slots in steps_
        if (steps_.size() > 0) {
            int last_step_count = steps_.get(steps_.size()-1);
            for (int i = (steps_.size() - 1); i < index; i++) {
                steps_.add(last_step_count);
            }
        }
    }

    private void addSteps(long current_time, int new_step_count) {
        // Calculate index for current_time
        int index = (int)((current_time - start_time_)/dtime_); // this is ugly...

        // Fill empty time slots in steps_
        int last_step_count = steps_.size() == 0 ? new_step_count : steps_.get(steps_.size()-1);
        if (steps_.size()>0) {
            for (int i = (steps_.size() - 1); i < index; i++) {
                steps_.add(last_step_count);
            }
        }

        // Append new cumulative step count to steps_ with condition for empty steps
        steps_.add(new_step_count);
    }

    private boolean playing_ = false;
    Random _rand = new Random();

    // play_audio plays a random audio file from a category
    private void play_audio(String category) {
        // Get list of available files
        Field[] fields = R.raw.class.getFields();

        // Select files which match coach and category
        String prefix = coach_ + "_" + category;
        ArrayList<Field> selected = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().substring(0, prefix.length()).equals(prefix)) {
                selected.add(fields[i]);
            }
        }
        if (selected.size() == 0) return;

        // Choose random audio file from list of selected files
        Field field = selected.get(_rand.nextInt(selected.size()));
        Context context = getApplicationContext();
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, field.getInt(field));
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    playing_ = false;
                }
            });
            mediaPlayer.start();
            playing_ = true;
        } catch (Exception e) {
            Log.e(TAG, "play_audio.MediaPlayer: " + e.toString());
        }
    }

    // decide_category uses steps data collected so far to pick a audio category
    private String decide_category(long current_time) {
        //  Calculate metrics
        //       - distance left (target_dist - k * steps so far)
        int new_step_count = 0;
        int left_dist = target_dist_;
        if (steps_.size()!=0) {
            new_step_count = steps_.get(steps_.size()-1);
            left_dist = target_dist_-new_step_count+steps_.get(0);
        }


        //       - current velocity (over past 10s)
        float vel = 0;
        if (steps_.size()>21)   {
            // at 500ms per entry, 10s corresponds to 20 entries ago
            vel = (float)(new_step_count - steps_.get(steps_.size()-21));
            vel = vel/10; // in m/s
            Log.d(TAG, "Step count 10s ago: "+ steps_.get(steps_.size()-21));
        }

        //       - current acceleration (over past 10s) - Need to store velocities
        //       - time left
        int elapsed_time = (int)(current_time-start_time_);
        int left_time = target_time_-elapsed_time;
        //       - avg. velocity needed to reach goal
        double target_vel = left_dist*1e3 / left_time; // in m/s


        // Normalize metrics by target, in percent (acc does not need to be normalized)
        int vel_normalized = (int)(100*vel/target_vel);
        int time_normalized = (int)(100*(elapsed_time)/(target_time_));

        // Associate metrics with category
        // TODO : more categories
        String category="all_good";
        if (vel_normalized > 120) category="too_fast";
        if (vel_normalized < 60) category ="too_slow";
        if (vel_normalized < 40) category="stopping";

        Log.d(TAG, "Normalized velocity: "+ vel_normalized);
        Log.d(TAG, "Category picked: " + category);

        return category; // TODO: remove
    }
}
