package chopparun.hackzurich.com.trainer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GoalEntry extends AppCompatActivity {

    public final static String GOAL_TIME = "com.chopparun.GOAL_TIME";
    public final static String GOAL_DISTANCE = "com.chopparun.GOAL_DIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_entry);

        Button startButton = (Button) findViewById(R.id.buttonStart);
    }

    public void startRunning(View view){
        Intent intent = new Intent(this, RunningActivity.class);

        EditText distanceInputView = (EditText) findViewById(R.id.goal_distance_input);
        EditText timeInputView = (EditText) findViewById(R.id.goal_time_input);
        int distance = Integer.valueOf(distanceInputView.getText().toString());
        int time = Integer.valueOf(timeInputView.getText().toString());

        intent.putExtra(GOAL_DISTANCE, distance);
        intent.putExtra(GOAL_TIME, time);

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_goal_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
