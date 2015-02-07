package rodion.izotov.donetsk.tronestclock;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class ClockScreen extends Activity {

    private TextView textWithTime;
    private DateFormat timeFormat;
    private Timer mainClock;

    private final Runnable drawRunnable = new Runnable() {
        @Override
        public void run() {
            ClockScreen.this.onUpdateTime();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_clock_screen);


        Locale locale = getResources().getConfiguration().locale;
        textWithTime = (TextView)findViewById(R.id.text_for_time);
        timeFormat = new SimpleDateFormat("HH:mm:ss", locale);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "tron.ttf");
        textWithTime.setTypeface(myTypeface);

        onUpdateTime();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (mainClock == null) {
            mainClock = new Timer();
            mainClock.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    ClockScreen.this.clockTick();
                }
            }, 0, 1000);
        }
    }

    @Override
    protected void onPause(){

        super.onPause();

        mainClock.cancel();
        mainClock = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_clock_screen, menu);
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

    private void clockTick() {

        if (ClockScreen.this.textWithTime == null)
            throw new NullPointerException("An view is null");

        ClockScreen.this.textWithTime.post(drawRunnable);
    }

    private void onUpdateTime() {
        textWithTime.setText(timeFormat.format(new Date()));
    }

    public Handler mainHandler = new Handler() {

        public void handleMessage(Message myMsg) {
            ClockScreen.this.onUpdateTime();
        }

        @Override
        public void close() {

        }

        @Override
        public void flush() {

        }

        @Override
        public void publish(LogRecord record) {

        }
    };
}
