package chopparun.hackzurich.com.trainer;

import android.app.Service;
import android.content.BroadcastReceiver;
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

import chopparun.hackzurich.com.trainer.RunTrackerService.LocalBinder;

public class RunningActivity extends AppCompatActivity {
    private final String TAG = "RunningActivity";
    private TextView TimeElapsed,StepsElapsed;

    private String coach_;
    private int target_time_;
    private int target_dist_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        coach_ = intent.getStringExtra(GoalEntry.TRAINER);
        target_time_ = intent.getIntExtra(GoalEntry.GOAL_TIME, 1) * 60000;
        target_dist_ = intent.getIntExtra(GoalEntry.GOAL_DISTANCE, 150);

        setContentView(R.layout.activity_running);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start RunTrackerService if not started
        Intent intent = new Intent(this, RunTrackerService.class);
        Log.d(TAG, "Calling bindService");
        Bundle mBundle = new Bundle();
        mBundle.putLong(GoalEntry.GOAL_DISTANCE,targetDistance);
        mBundle.putLong(GoalEntry.GOAL_TIME,targetTime);
        intent.putExtras(mBundle);

        startService(intent);
        bindService(intent, connection_, Context.BIND_AUTO_CREATE);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound_ = false;
        }
    };

}
