package eu.mvanco.lila;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import eu.mvanco.camera.PhotoIntentActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserPanelActivity extends Activity {
	String sessionID;
	TextView user;
	EditText fellow;
	Button add_fellow_button;
	Button show_wall_button;
	Button on_run_background;
	Button upload_pictures_button;
	private PendingIntent mAlarmSender;
	StringBuffer fileContent;
	
	InputStream is;
	
	AlarmManager am;
	
	public static String BASE_URL = "http://mvanco.eu/index.php/action/";
	
	final Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_panel);
		
		sessionID = getIntent().getStringExtra(MainActivity.SESSION_ID);

		
		user = (TextView) findViewById(R.id.panel_user);
		fellow = (EditText) findViewById(R.id.panel_fellow);
		
		add_fellow_button = (Button) findViewById(R.id.panel_add_fellow_button);
		add_fellow_button.setOnClickListener(add_fellow_listener);
		
		
		show_wall_button = (Button) findViewById(R.id.panel_show_wall_button);
		show_wall_button.setOnClickListener(show_wall_listener);
		
		upload_pictures_button = (Button) findViewById(R.id.panel_upload_pictures_button);
		upload_pictures_button.setOnClickListener(upload_pictures_listener);

		
		
		on_run_background = (Button) findViewById(R.id.panel_run_on_background_button);
		on_run_background.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startTestDailyChallenge();
				Intent startMain = new Intent(Intent.ACTION_MAIN);
				startMain.addCategory(Intent.CATEGORY_HOME);
				startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(startMain);
			}
			
		});
		
		setUserName();
		
		
        mAlarmSender = PendingIntent.getService(UserPanelActivity.this,
                0, new Intent(UserPanelActivity.this, BgService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        
		
	}
	
	OnClickListener add_fellow_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (fellow.getText().toString().isEmpty()) {
				Toast.makeText(UserPanelActivity.this, "Type fellow name in text field!", Toast.LENGTH_LONG).show();
				return;
			}
			
			new Thread(addFellowRequest).start();
		}
		
	};
	
	OnClickListener show_wall_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent showWallIntent = new Intent(UserPanelActivity.this, WallActivity.class);
			
			String url = BASE_URL + "show_wall/" + sessionID;
			showWallIntent.putExtra(WallActivity.URL, url);
			
			startActivity(showWallIntent);
		}
		
	};
	
	OnClickListener upload_pictures_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			FileInputStream stream = null;
			try {
				stream = openFileInput(PhotoIntentActivity.COMMIT_FILE);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fileContent = new StringBuffer("");

			byte[] buffer = new byte[1024];

			try {
				while (stream != null && stream.read(buffer) != -1) {
				    fileContent.append(new String(buffer));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		    new Thread(new Runnable() {
		        //Thread to stop network calls on the UI thread
		        public void run() {
	                uploadFromCommitString(fileContent.toString());
	                mHandler.post(uploadedPicturesMessage);
		        }
		    }).start();
			
			
			File dir = getFilesDir();
			File file = new File(dir, PhotoIntentActivity.COMMIT_FILE);
			file.delete();
			
		}
			
	};
	
	private boolean uploadFromCommitString(String commitString) {
		if (commitString != null && commitString.length() > 3) {
			int endIndex = commitString.lastIndexOf(";");
			String newstr = null;
		    if (endIndex != -1)  {
		    	newstr = new String(commitString.substring(0, endIndex));
		    }
		    
		    if (newstr == null)
		    	return true;
		
			for (String dailyChallenge : newstr.split(";")) {
				if (dailyChallenge.isEmpty())
					continue;
				
				
				String[] fieldsOfDC = dailyChallenge.split("\\{");
				
		        Bitmap bitmapOrg = BitmapFactory.decodeFile(fieldsOfDC[0]);
	     
	            ByteArrayOutputStream bao = new ByteArrayOutputStream();
	     
	            bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 90, bao);
	     
	            byte[] ba = bao.toByteArray();
	     
	            String ba1 = Base64.encodeBytes(ba);
	           
	            /*
	            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	            try {
	                reqEntity.addPart("Picture", new StringBody(ba1));
	                reqEntity.addPart("Description", new StringBody(fieldsOfDC[1]));
	                reqEntity.addPart("Session_ident", new StringBody(sessionID));
	                reqEntity.addPart("Latitude", new StringBody(""));
	                reqEntity.addPart("Longitude", new StringBody(""));
	                reqEntity.addPart("Date_time", new StringBody(fieldsOfDC[2]));
	            } catch (UnsupportedEncodingException e) {
	                e.printStackTrace();
	            }
	            */
	            
	            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
	             
                nameValuePairs.add(new BasicNameValuePair("Picture", ba1));
                nameValuePairs.add(new BasicNameValuePair("Description", fieldsOfDC[1]));
                nameValuePairs.add(new BasicNameValuePair("Session_ident", sessionID));
                nameValuePairs.add(new BasicNameValuePair("Date_time", fieldsOfDC[2]));
                nameValuePairs.add(new BasicNameValuePair("Latitude", "x"));
                nameValuePairs.add(new BasicNameValuePair("Longitude", "x"));
                
                if (fieldsOfDC[5].compareTo("x") != 0) {
                    nameValuePairs.add(new BasicNameValuePair("Address", fieldsOfDC[5]));
                }
                
                if (fieldsOfDC[3].compareTo("x") != 0 && fieldsOfDC[4].compareTo("x") != 0) {
                	nameValuePairs.add(new BasicNameValuePair("Latitude", fieldsOfDC[3]));
                	nameValuePairs.add(new BasicNameValuePair("Longitude", fieldsOfDC[4]));
                }

	     
	            try {
	     
	                HttpClient httpclient = new DefaultHttpClient();
	     
	                HttpPost httppost = new
	     
	                HttpPost(BASE_URL + "uploadDailyChallenge");
	    
	                //httppost.setEntity(reqEntity);
	                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	     
	                HttpResponse response = httpclient.execute(httppost);
	                
	                int responsecode = response.getStatusLine().getStatusCode();
	     
	                HttpEntity entity = response.getEntity();
	     
	                is = (InputStream) entity.getContent();
	                //is = (InputStream) entity.getContent();
	     
	            } catch (Exception e) {
	     
	                Log.e("log_tag", "Error in http connection " + e.toString());
	     
	            }
			}
		
		}
		return true;
	}
	
	final Runnable sendAddFellowPositiveFeedback = new Runnable() {
	    public void run() {
	    	Toast.makeText(UserPanelActivity.this, "Friend has been added", Toast.LENGTH_LONG).show();
	    }
	};
	
	final Runnable sendAddFellowNegativeFeedback = new Runnable() {
	    public void run() {
	    	Toast.makeText(UserPanelActivity.this, "Error: Friend can't be added! Username is probably not registered.", Toast.LENGTH_LONG).show();
	    }
	};

	final Runnable uploadedPicturesMessage = new Runnable() {
	    public void run() {
	    	Toast.makeText(UserPanelActivity.this, "Pictures has been uploaded", Toast.LENGTH_LONG).show();
	    }
	};
	
	Runnable addFellowRequest = new Runnable() {

		@Override
		public void run() {
			StringBuilder url = new StringBuilder();
			url.append("http://mvanco.eu/index.php/action/add_friend");
			url.append("/" + sessionID);
			url.append("/" + fellow.getText().toString());
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

                if (statusLine.getStatusCode() == 201) {
                	mHandler.post(sendAddFellowPositiveFeedback);
                }
                else {
                	mHandler.post(sendAddFellowNegativeFeedback);
                }
                
			}
			else {
				mHandler.post(sendAddFellowNegativeFeedback);
			}
		}
		
	};
	
	public void setUserName() {
		
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				final String firstName = executeRequest("getUserInfo", sessionID, "First_name");
				final String familyName = executeRequest("getUserInfo", sessionID, "Family_name");
				
				if (firstName != null && familyName != null) {
					UserPanelActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							user.setText(firstName + " " + familyName);
							
						}
					});
				}
				
			}
			
		}).start();

		

		
	}
	
	public static String executeRequest(String funcString, String firstParam, String secondParam) {
		StringBuilder url = new StringBuilder();
		url.append(BASE_URL + funcString);
		url.append("/" + firstParam);
		url.append("/" + secondParam);
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

            if (statusLine.getStatusCode() == 201) {
            	
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
				return content;
            	
            }
            else {
            	return null;
            }
            
		}
		else {
			return null;
		}
	}
	
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
    
    
    
    public void startTestDailyChallenge() {
        // We want the alarm to go off 30 seconds from now.
        Calendar now = Calendar.getInstance();
        


        // Schedule the alarm!
        am = (AlarmManager)getSystemService(ALARM_SERVICE);
           
        PendingIntent startDailyChallenge = PendingIntent.getBroadcast(UserPanelActivity.this,
                0, new Intent(UserPanelActivity.this, DailyChallengeReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        
        am.set(AlarmManager.RTC, now.getTimeInMillis() + 5000, startDailyChallenge);
        
        /* this will be final version */ /*
        
        firstTime.set(Calendar.SECOND, 0);
        firstTime.set(Calendar.MINUTE, 59);
        firstTime.set(Calendar.HOUR_OF_DAY, 23);

    	long firstTimeinMillis = firstTime.getTimeInMillis();

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        
        //this set to INTERVAL_HOUR
        am.setRepeating(AlarmManager.RTC,
        		firstTimeinMillis, AlarmManager.INTERVAL_DAY, mAlarmSender);
        
        */ /* end of final version */
        

        // Tell the user about what we did.
        //Toast.makeText(MainActivity.this, firstTime.format("%d.%m. %Y %H:%M:%S"),
        //        Toast.LENGTH_LONG).show();
    };
    
    private void stopAlarm() {
    	
    	
        Intent dailyChallengeIntent = new Intent(this, DailyChallengeReceiver.class);
        dailyChallengeIntent.putExtra(DailyChallengeReceiver.FINISH_DAILY_CHALLENGE, true);
    	sendBroadcast(dailyChallengeIntent);
    }
    
    private void stopWholeAlarmPlanning() {
    	am.cancel(mAlarmSender);
    }
	
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	/*new Thread(new Runnable() {
	        public void run() {
	        	String firstName = executeRequest("getUserInfo", sessionID, "First_name");
	        	String familyName = executeRequest("getUserInfo", sessionID, "Family_name");
	        	user.setText(firstName + " " + familyName);
                
	        }
	    }).start();*/
    	
    	
    	user.setText("User Panel");
    	
  
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_panel, menu);
		return true;
	}

}
