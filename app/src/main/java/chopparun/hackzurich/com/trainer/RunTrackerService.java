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

import chopparun.hackzurich.com.trainer.utilities._LocationListener;


/*
    RunTrackerService is started and stopped by RunningActivity
 */
public class RunTrackerService extends Service {
    // TAG for use in logging
    private static final String TAG = "RunTrackerService";

    //----------------------------------------------------------------------------------------------

    /* Audio related */
    private String coach_ = "???"; // Current selected coach

    /* Steps counting related */
    ArrayList<Integer> steps_ ;// All steps accumulated. New cumulative step counts are appended.
    private long start_time_;  // Timestamp for value at steps_[0] (in ms, new Date().getTime())
    private long dtime_ = 500; // ms between each entry in steps_[]
    private static final long interval_ = 5000; // window width (ms)

    /*
        Personalization related
        - Take onCreate from RunningActivity (input in GoalEntry)
     */
    private long target_time_ = 600000; // in ms
    private long target_dist_ = 1000;   // in meters

    //----------------------------------------------------------------------------------------------

    public void setCoach(String coach) { coach_ = coach; }
    public void setTime(long target) { target_time_ = target; }
    public void setDist(long target) { target_dist_ = target; }
    public float getDist(){
        return total_dist_;
    }
    public long getTime(){return (new Date().getTime()-start_time_);}
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

    private Accelerometer accelerometer_;
    private _LocationListener location_listener_;
    private Timer timer_ = new Timer();

    // Only called when service is started
    @Override
    public void onCreate() {
        // Get accelerometer class instance
        accelerometer_ = new Accelerometer(this);

        // Get _LocationListener
        //location_listener_ = new _LocationListener(this);

        // TODO: Make run in foreground

        steps_= new ArrayList<>();
        start_time_ = new Date().getTime();

        /*
        timer_.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                check();
            }
        }, new Date(), 3000);
        */
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy called");
        super.onDestroy();
        accelerometer_.onDestroy();
        //location_listener_.onDestroy();

        if (media_player_ != null) {
            media_player_.release();
        }

        timer_.cancel();

        stopSelf();
    }

    private float total_dist_ = 0.f;
    public void updateDistance(float delta) {
        total_dist_ += delta;
        Log.i(TAG, "Total distance: " + String.valueOf(total_dist_));
    }

    // Main pipeline
    public void onStepCount(int new_step_count) {
        Log.d(TAG, "new_step_count = " + String.valueOf(new_step_count));

        long current_time = new Date().getTime();
        addSteps(current_time, new_step_count);

        total_dist_ += 1;//0.65;

        check();
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
        if (index + 1 == steps_.size()) steps_.set(index, new_step_count);
        else steps_.add(new_step_count);
    }

    private MediaPlayer media_player_ = null;
    private boolean playing_ = false;
    int previous_track_;
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
        if (media_player_ != null) media_player_.release();

        int next_track_;
        try {
            next_track_ = field.getInt(field);
            if (next_track_ == previous_track_) return;

            Context context = getApplicationContext();
            media_player_ = MediaPlayer.create(context, next_track_);
            media_player_.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    playing_ = false;
                }
            });
            media_player_.start();
            playing_ = true;
            previous_track_ = next_track_;
        } catch (Exception e) {
            Log.e(TAG, "play_audio.MediaPlayer: " + e.toString());
        }
    }

    // decide_category uses steps data collected so far to pick a audio category
    private String decide_category(long current_time) {
        //  Calculate metrics
        long left_time = 0 ,left_dist = 0;
        double target_vel = 0.0;
        int time_normalized, vel_normalized;

        //       - distance left (target_dist - k * steps so far)
        final int steps_n = steps_.size();
        int new_step_count = steps_n > 0 ? steps_.get(steps_n - 1) : 0;
        //       - time left
        long elapsed_time = current_time-start_time_;
        if (target_time_>0){
            left_time = target_time_-elapsed_time;
        }
        /*
        if (target_dist_>0){
            left_dist = target_dist_;
            if (steps_.size()!=0) {
                new_step_count = steps_.get(steps_.size()-1);
                left_dist = (int)(target_dist_- total_dist_);
            }

            //       - avg. velocity needed to reach goal
            target_vel = left_time > 0 ?
                    left_dist*1e3 / left_time : target_dist_ * 1e3 / target_time_; // in m/s
        }
        */
        target_vel = target_dist_ * 1000 / target_time_;


        //       - current velocity (over past interval s)
        float vel = 0;
        int steps_back =  (int) (interval_/dtime_);
        if (steps_.size()> steps_back+1)   {
            // at 500ms per entry, 10s corresponds to 20 entries ago
            vel = (float)(new_step_count - steps_.get(steps_.size()-steps_back));
            vel = vel/(interval_/1000); // in steps/s
            Log.d(TAG, "Step count " + interval_+ "s ago: "+ steps_.get(steps_.size()-steps_back));
        }
        Log.d(TAG, "target_vel: " + String.valueOf(target_vel) + ", vel: " + String.valueOf(vel));

        //      - current acceleration (over past 10s) - Need to store velocities


        // Normalize metrics by target, in percent (acc does not need to be normalized)
        vel_normalized = (int) (target_vel == 0? 0: (100*vel/target_vel));
        time_normalized = (int) (target_time_ == 0? 0: (100*elapsed_time/target_time_));

        // Flags to help understand metrics
        boolean speed_stopped = vel_normalized < 26;
        boolean speed_slow    = vel_normalized > 25 && vel_normalized < 86;
        boolean speed_good    = vel_normalized > 85 && vel_normalized < 111;
        boolean speed_fast    = vel_normalized > 110 && vel_normalized < 131;
        boolean speed_toofast = vel_normalized > 130;

        boolean time_start  = time_normalized < 31;
        boolean time_middle = time_normalized > 30 && time_normalized < 81;
        boolean time_end    = time_normalized > 80 && time_normalized < 101;
        boolean time_more   = time_normalized > 100;

        // Associate metrics with category
        String category = "all_good";
        if (speed_stopped) category = "stopping";
        else if (speed_slow) category = "too_slow";

        if (time_start) {
            if (speed_toofast) category = "too_fast";
            if (speed_fast) category = "perfect";
        }
        if (time_middle) {
            if (speed_good || speed_fast) category = "perfect";
            else if (speed_toofast) category = "too_fast";
        } else if (time_end) {
            if (speed_good) category = "all_good";
            else if (speed_fast || speed_toofast) category = "perfect";
        } else if (time_more) {
            if (speed_stopped) category = "finish";
            else if (speed_good || speed_slow) category = "all_good";
            else if (speed_fast || speed_toofast) category = "perfect";
        }
        Log.d(TAG, "Normalized velocity: "+ vel_normalized);
        Log.d(TAG, "Category picked: " + category);

        return category;
    }
}
