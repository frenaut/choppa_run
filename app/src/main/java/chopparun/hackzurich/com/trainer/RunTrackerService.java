package chopparun.hackzurich.com.trainer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Path;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.Date;


/*
    RunTrackerService is started and stopped by RunningActivity
 */
public class RunTrackerService extends Service {
    // TAG for use in logging
    private static final String TAG = "RunTrackerService";

    //----------------------------------------------------------------------------------------------

    /* Audio related */
    private String coach_ = "Arnie"; // Current selected coach

    /* Steps counting related */
    private int  steps_[];     // All steps accumulated. New cumulative step counts are appended.
    private long start_time_;  // Timestamp for value at steps_[0] (in ms, new Date().getTime())
    private int  dtime_ = 500; // ms between each entry in steps_[]

    /*
        Personalization related
        - Take onCreate from RunningActivity (input in GoalEntry)
     */
    private int target_time_; // in seconds
    private int target_dist_; // in meters

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
        // TODO: Make run in foreground
        // TODO: Initialize data structures
        // TODO: Start audio manager?
    }

    // Main pipeline
    public void onStepCount() {
        int new_step_count = 0; // TODO: Read out step count
        long current_time = new Date().getTime();
        String category;

        updateSteps_(current_time, new_step_count);

        category = decide_category(current_time);

        if (!category.isEmpty()) play_audio(category);
    }

    // Update steps_ list
    private void updateSteps_(long current_time, int new_step_count) {
        // TODO: Calculate index for current_time
        // TODO: Append new cumulative step count to steps_
        // TODO: OR update existing steps_[index]
        // TODO? Fill empty time slots in steps_ (interpolate?)
    }

    // play_audio plays a random audio file from a category
    private void play_audio(String category) {
        // TODO: Pick audio directory using `coach` and category
        // TODO: Choose random audio file from directory (For now just have a 01.mp3?)
    }

    // decide_category uses steps data collected so far to pick a audio category
    private String decide_category(long current_time) {
        // TODO: Calculate metrics
        //       - distance left (target_dist - k * steps so far)
        int new_step_count = 0;
        if (steps_.length!=0) {
            new_step_count = steps_[steps_.length-1];
        }
        int left_dist = target_dist_-new_step_count;

        //       - current velocity (over past 10s)
        double vel = 0;
        if (steps_.length>21)   {
            // at 500ms per entry, 10s corresponds to 20 entries ago
            vel = new_step_count - steps_[steps_.length-21];
            vel = vel/10; // in m/s
        }
        //       - current acceleration (over past 10s) - Need to store velocities
        //       - time left
        int left_time = (int)(start_time_-current_time)+target_time_;
        //       - avg. velocity needed to reach goal
        double target_vel = left_dist / (left_time*1e3);


        // TODO: Normalize metrics by target

        // TODO: Associate metrics with category

        return "Category"; // TODO: remove
    }
}
