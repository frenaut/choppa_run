package chopparun.hackzurich.com.trainer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import chopparun.hackzurich.com.trainer.utilities.ImageHelper;

public class GoalEntry extends AppCompatActivity {

    public final static String GOAL_TIME = "com.chopparun.GOAL_TIME";
    public final static String GOAL_DISTANCE = "com.chopparun.GOAL_DIST";
    public final static int NUM_TRAINER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView trainerPicker1 = (ImageView) findViewById(R.id.TrainerPicker1);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap head1 = BitmapFactory.decodeResource(getResources(), R.drawable.arnold_head, options);

        Bitmap head1_drawable = Bitmap.createScaledBitmap(head1,
                trainerPicker1.getLayoutParams().width, trainerPicker1.getLayoutParams().width, false);

        head1_drawable = ImageHelper.getRoundedCornerBitmap(head1_drawable);

        trainerPicker1.setImageBitmap(head1_drawable);
    }

    public void startRunning(View view){
        Intent intent = new Intent(this, RunningActivity.class);

        EditText distanceInputView = (EditText) findViewById(R.id.goal_distance_input);
        EditText timeInputView = (EditText) findViewById(R.id.goal_time_input);
        String tmp = distanceInputView.getText().toString();
        int distance = tmp.isEmpty()? 0:Integer.valueOf(tmp);
        tmp = timeInputView.getText().toString();
        int time = tmp.isEmpty()? 0:Integer.valueOf(tmp);

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
