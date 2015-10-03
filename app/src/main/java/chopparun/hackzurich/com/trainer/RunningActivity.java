package chopparun.hackzurich.com.trainer;

import android.app.Service;
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
    private long targetTime;
    private long targetDistance;
    private TextView TimeElapsed,StepsElapsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        targetTime = (long) this.getIntent().getIntExtra(GoalEntry.GOAL_DISTANCE,0)*60000;
        targetDistance = (long) this.getIntent().getIntExtra(GoalEntry.GOAL_TIME,0);
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
    private Service service_;
    private ServiceConnection connection_ = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            service_ = binder.getService();
            bound_ = true;
            Log.d(TAG, "Bound to RunTrackerService");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound_ = false;
        }
    };

}
