package chopparun.hackzurich.com.trainer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import chopparun.hackzurich.com.trainer.utilities.ImageHelper;

public class GoalEntry extends AppCompatActivity {

    private static final String TAG = "GoalEntry";

    public final static String GOAL_TIME = "com.chopparun.GOAL_TIME";
    public final static String GOAL_DISTANCE = "com.chopparun.GOAL_DIST";
    public final static String TRAINER = "com.chopparun.TRAINER";

    public int curr_Picker = R.id.Picker_Arnie;
    public static String coach = "ARNIE";

    public static int SCREEN_WIDTH;
    public static int IMG_WIDTH, IMG_HEIGHT,LARGE_IMG_WIDTH,LARGE_IMG_HEIGHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH = size.x;
        IMG_WIDTH = SCREEN_WIDTH/5;
        IMG_HEIGHT = SCREEN_WIDTH/5;
        LARGE_IMG_WIDTH = SCREEN_WIDTH/3;
        LARGE_IMG_HEIGHT = SCREEN_WIDTH/3;

        setContentView(R.layout.activity_goal_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // pick arnie as default
        ImageView arnie = (ImageView) findViewById(R.id.Picker_Arnie);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        Bitmap head_arnie = BitmapFactory.decodeResource(getResources(), R.drawable.arnie_head, options);
        Bitmap head_arnie_drawable = ImageHelper.getRoundedCornerBitmap(head_arnie);
        Bitmap.createScaledBitmap(head_arnie_drawable,LARGE_IMG_WIDTH,LARGE_IMG_WIDTH,false);
        arnie.setImageBitmap(head_arnie_drawable);

        // pick arnie as default
        ImageView sammi = (ImageView) findViewById(R.id.Picker_Sammi);
        Bitmap head_sammi = BitmapFactory.decodeResource(getResources(), R.drawable.sammi_head, options);
        Bitmap head_sammi_drawable = ImageHelper.getRoundedCornerBitmap(head_sammi);
        Bitmap.createScaledBitmap(head_sammi_drawable,IMG_WIDTH,IMG_WIDTH,false);
        sammi.setImageBitmap(head_sammi_drawable);
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
        intent.putExtra(TRAINER, coach);

        startActivity(intent);
    }
    public void pickTrainer(View view){

        int trainer = view.getId();
        Log.i(TAG, ""+trainer);
        switch (trainer){
            case R.id.Picker_Arnie:
                coach = "ARNIE";
                break;
            case R.id.Picker_Sammi:
                coach = " SAMMI";
                break;
            case R.id.Picker_3:
                coach = " ";
                break;
        }
        // enlarge selected picker
        view.getLayoutParams().width = LARGE_IMG_WIDTH; view.getLayoutParams().height = LARGE_IMG_HEIGHT;
        view.setLayoutParams(new RelativeLayout.LayoutParams(LARGE_IMG_WIDTH,LARGE_IMG_HEIGHT));
        findViewById(curr_Picker).setLayoutParams(new RelativeLayout.LayoutParams(IMG_WIDTH, IMG_HEIGHT));
        curr_Picker = trainer;
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
