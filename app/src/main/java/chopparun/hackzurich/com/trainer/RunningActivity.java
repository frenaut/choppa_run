package chopparun.hackzurich.com.trainer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import java.util.Timer;
import java.util.TimerTask;

import chopparun.hackzurich.com.trainer.RunTrackerService.LocalBinder;

public class RunningActivity extends AppCompatActivity {
    private final String TAG = "RunningActivity";
    private TextView timeElapsed,stepsElapsed;

    private String coach_;
    private long target_time_;
    private long target_dist_;
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        coach_ = intent.getStringExtra(GoalEntry.TRAINER);
        target_time_ = (long) intent.getIntExtra(GoalEntry.GOAL_TIME, 1) * 60000;
        target_dist_ = (long) intent.getIntExtra(GoalEntry.GOAL_DISTANCE, 150);

        setContentView(R.layout.activity_running);

        stepsElapsed = (TextView) findViewById(R.id.distance_elapsed);
        timeElapsed = (TextView) findViewById(R.id.time_elapsed);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start RunTrackerService if not started
        Intent intent = new Intent(this, RunTrackerService.class);
        startService(intent);

        if (!bound_) {
            Log.d(TAG, "bindService onStart");
            bindService(intent, connection_, Context.BIND_AUTO_CREATE);
        }
        timer.scheduleAtFixedRate(new UpdateTimeTask(), 500, 1000);
    }

    class UpdateTimeTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stepsElapsed.setText(String.valueOf((int)service_.getDist())+" m");
                    String timeString = GetFormattedInterval(service_.getTime());
                    timeElapsed.setText(timeString);
                }
            });
        }
    }

    public static String GetFormattedInterval(final long ms) {
        long x = ms / 1000;
        long seconds = x % 60;
        x /= 60;
        long minutes = x ;

        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bound_) {
            Log.d(TAG, "bindService onResume");
            Intent intent = new Intent(this, RunTrackerService.class);
            bindService(intent, connection_, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void StopRunning(View view){
        if(bound_){
          unbindService(connection_);
        }
        Intent intent = new Intent(this, RunTrackerService.class);
        stopService(intent);
        startActivity(new Intent(this, GoalEntry.class));
    }


    private boolean bound_ = false;
    private RunTrackerService service_;
    private ServiceConnection connection_ = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            service_ = binder.getService();
            service_.setCoach(coach_);
            service_.setDist(target_dist_);
            service_.setTime(target_time_);
            Log.d(TAG, "Will run for coach " + coach_ + " and target distance/time: " + target_dist_ +
                "/" + target_time_);
            bound_ = true;
            Log.d(TAG, "Bound to RunTrackerService");

            service_.play_audio("start");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound_ = false;
        }
    };

    /*private Runnable mRunnable = new Runnable() {
        public void run() {
            updateCounter();
            mHandler.postDelayed(this, 1000);
        }
    };*/



}
