package eu.mvanco.lila;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import eu.mvanco.camera.PhotoIntentActivity;
import eu.mvanco.lila.settings.MyPrefsActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity of application. Shows buttons to start and stop background service
 * and contains menu for settings. Starts background service BgService...
 * 
 * @author Matus
 *
 */
public class MainActivity extends Activity
{
	
	static final int GET_USERNAME_REQUEST = 1;  // The request code
	public static String SESSION_ID = "sessionID";
	Handler mHandler;
	
	private PendingIntent mAlarmSender;
	private PendingIntent fictionalSender;
	private Button registrationButton;
	private Button loginButton;
	private EditText username;
	private EditText password;
	SharedPreferences settings;
	
	final String FIRST_TIME_RUN = "eu.mvanco.lila.first_time_run";
	
	AlarmManager am;
	
	final Runnable loginError = new Runnable() {
	    public void run() {
	    	Toast.makeText(MainActivity.this, "Wrong credentials or missing internet connection. Try again", Toast.LENGTH_LONG).show();
        	username.setText("");
        	password.setText("");
    	}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHandler = new Handler(Looper.getMainLooper());
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Create an IntentSender that will launch our service, to be scheduled
        // with the alarm manager.w
        mAlarmSender = PendingIntent.getService(MainActivity.this,
                0, new Intent(MainActivity.this, BgService.class), 0);
      
        

        
        setContentView(R.layout.login_screen);
        
        registrationButton = (Button) findViewById(R.id.registration_button);
        loginButton = (Button) findViewById(R.id.login_button);
        
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        
        registrationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, RegistrationActivity.class);
				startActivityForResult(i, GET_USERNAME_REQUEST);
			}
        	
        });
        
        loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			    new Thread(loginRunnable).start();
				
			}
        	
        });

        // Watch for button clicks.
        //Button button = (Button)findViewById(R.id.start_alarm);
        //button.setOnClickListener(mStartAlarmListener);
        //button = (Button)findViewById(R.id.stop_alarm);
        //button.setOnClickListener(mStopAlarmListener);
        
        TextView tv = (TextView) findViewById(R.id.panel_user);
        tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		    	startPlanningService();
		    	
		    	Toast.makeText(MainActivity.this, "!Secret functionality: BgService has been called at this time due to testing purposes", Toast.LENGTH_SHORT).show();
			}
        	
        });
    }
    
    Runnable loginRunnable = new Runnable() {
        //Thread to stop network calls on the UI thread
        public void run() {
			
			StringBuilder url = new StringBuilder();
			url.append("http://mvanco.eu/index.php/action/login");
			url.append("/" + username.getText().toString());
			url.append("/" + password.getText().toString());
			HttpGet getRequest = new HttpGet(url.toString());
			
			HttpClient httpclient = new DefaultHttpClient();
		    HttpResponse response = null;
        	
			try {
				response = httpclient.execute(getRequest);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (response != null) {
				StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == 201 || statusLine.getStatusCode() == 200) {
                	Intent i = new Intent(MainActivity.this, UserPanelActivity.class);
                	
                	String content = null;
					try {
						content = convertStreamToString(response.getEntity().getContent());
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	i.putExtra(SESSION_ID, content);
                	
                	
                	

                	
        			String filename = "first_time_run_token";
        			StringBuffer fileContent;
                	FileInputStream inputStream = null;
        			try {
        				inputStream = openFileInput(filename);
        			} catch (FileNotFoundException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
        			
        			fileContent = new StringBuffer("");

        			byte[] buffer = new byte[1024];

        			try {
        				while (inputStream != null && inputStream.read(buffer) != -1) {
        				    fileContent.append(new String(buffer));
        				}
        			} catch (IOException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                	
                	
                	if (fileContent.toString().isEmpty()) {
                	    //the app is being launched for first time, do something        
                	    Log.d("Comments", "First time");
                	    startDailyChallengeNotifications();
                	             // first time task
                		
                		String string = "daily_challenges_are_running";
                		FileOutputStream outputStream;
                	    // record the fact that the app has been started at least once
                		try {
                			  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                			  outputStream.write(string.getBytes());
                			  outputStream.close();
            			} catch (Exception e) {
                			  e.printStackTrace();
            			}
                	}
                
                	
                	startActivity(i);
                	
                }
                else {
                	mHandler.post(loginError);
                }
                
			}
			else {
				mHandler.post(loginError);
			}
            
        }
    };
    
    public void onSettingsClick(View v) {
    	startActivity(new Intent(MainActivity.this, MyPrefsActivity.class));
    }
    

    
    public void onLilaImageClick(View v) {
		FileInputStream stream = null;
		try {
			stream = openFileInput("analysis_file");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuffer fileContent = new StringBuffer("");

		byte[] buffer = new byte[1024];

		try {
			while (stream != null && stream.read(buffer) != -1) {
			    fileContent.append(new String(buffer));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(this);
        String imaginaryDC = userDetails.getString(BgService.IMAGINARY_DAILY_CHALLENGE, "");
		
        String message = null;
		if (fileContent.toString().isEmpty()) {
			message = "!Secret functionality: The next Daily Challenge has not been scheduled yet";
		}
		else {
			message = "!Secret functionality: The next Daily Challenge event is scheduled for " + fileContent.toString() + " next day";
		}
		
		if (!imaginaryDC.isEmpty()) {
			message = message + ", imaginary for " + imaginaryDC + "next day";
		}
		
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    public void startPlanningService() {
    	
    	am = (AlarmManager)getSystemService(ALARM_SERVICE);

		Calendar firstTime = Calendar.getInstance();
		
        Intent i = new Intent(MainActivity.this, BgService.class);
        i.putExtra(BgService.IMAGINARY_DAILY_CHALLENGE, true);
    	
        am.set(AlarmManager.RTC, firstTime.getTimeInMillis() + 10, PendingIntent.getService(MainActivity.this, 0, i, PendingIntent.FLAG_ONE_SHOT));
    	
    }
    
    
    public void startDailyChallengeNotifications() {
    	
    	
        // We want the alarm to go off 30 seconds from now.
        Calendar firstTime = Calendar.getInstance();
        


        // Schedule the alarm!
        am = (AlarmManager)getSystemService(ALARM_SERVICE);
        
        am.cancel(mAlarmSender);
        
        //this set to INTERVAL_HOUR
        //am.set(AlarmManager.RTC, firstTime.getTimeInMillis(), mAlarmSender);
        
        /* this will be final version */
        
        firstTime.set(Calendar.SECOND, 0);
        firstTime.set(Calendar.MINUTE, 59);
        firstTime.set(Calendar.HOUR_OF_DAY, 23);

    	long firstTimeinMillis = firstTime.getTimeInMillis();

        // Schedule the alarm!
        
        //this set to INTERVAL_HOUR
        am.setRepeating(AlarmManager.RTC,
        		firstTimeinMillis, AlarmManager.INTERVAL_DAY, mAlarmSender);
        
        /* end of final version */
        

        // Tell the user about what we did.
        //Toast.makeText(MainActivity.this, firstTime.format("%d.%m. %Y %H:%M:%S"),
        //        Toast.LENGTH_LONG).show();
    };
    
    public static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == GET_USERNAME_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
            	username.setText(data.getStringExtra(RegistrationActivity.USERNAME));
            	password.setText("");
            }
            else {
            	Toast.makeText(this, "Error during registration, try again with other values or check your internet connection.", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.action_registration:
				Intent i = new Intent(MainActivity.this, RegistrationActivity.class);
				startActivityForResult(i, GET_USERNAME_REQUEST);
	        	return true;
            case R.id.action_settings:
            	//Toast.makeText(this, "mytext", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, MyPrefsActivity.class));
                return true;

            default:
                return false;
        }
    }
    
    
}
