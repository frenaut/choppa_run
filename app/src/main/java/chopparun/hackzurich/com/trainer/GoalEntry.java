package chopparun.hackzurich.com.trainer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.GpsStatus;
import android.location.LocationManager;
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

    private final static String GOAL_TIME = "com.chopparun.GOAL_TIME";
    private final static String GOAL_DISTANCE = "com.chopparun.GOAL_DIST";
    private final static String TRAINER = "com.chopparun.TRAINER";

    private int curr_Picker = R.id.Picker_Arnie;
    private static String coach = "ARNIE";

    protected static int SCREEN_WIDTH;
    protected static int IMG_WIDTH, IMG_HEIGHT,LARGE_IMG_WIDTH,LARGE_IMG_HEIGHT;

    private Bitmap head_arnie_drawable,head_sammi_drawable;
    private ImageView arnie,sammi;

    public LocationManager locationManager;
    public GpsStatus gpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // get screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH = size.x;
        IMG_WIDTH = SCREEN_WIDTH/5;
        IMG_HEIGHT = SCREEN_WIDTH/5;
        LARGE_IMG_WIDTH = SCREEN_WIDTH/3;
        LARGE_IMG_HEIGHT = SCREEN_WIDTH/3;

        Log.i(TAG,""+LARGE_IMG_WIDTH+IMG_WIDTH);
        setContentView(R.layout.activity_goal_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;

        // pick arnie as default
        arnie = (ImageView) findViewById(R.id.Picker_Arnie);
        Bitmap head_arnie = BitmapFactory.decodeResource(getResources(), R.drawable.arnie_head, options);
        head_arnie_drawable = ImageHelper.getRoundedCornerBitmap(head_arnie);
        arnie.setImageBitmap(Bitmap.createScaledBitmap(head_arnie_drawable, LARGE_IMG_WIDTH, LARGE_IMG_WIDTH, false));;

        // pick arnie as default
        sammi = (ImageView) findViewById(R.id.Picker_Sammi);
        Bitmap head_sammi = BitmapFactory.decodeResource(getResources(), R.drawable.sammi_head, options);
        head_sammi_drawable = ImageHelper.getRoundedCornerBitmap(head_sammi);
        sammi.setImageBitmap(Bitmap.createScaledBitmap(head_sammi_drawable, IMG_WIDTH, IMG_WIDTH, false));
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
        Log.i(TAG, ""+view.getWidth());
        Log.i(TAG, ""+findViewById(curr_Picker).getWidth());
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
        view.setLayoutParams(new RelativeLayout.LayoutParams(LARGE_IMG_WIDTH, LARGE_IMG_HEIGHT));
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
