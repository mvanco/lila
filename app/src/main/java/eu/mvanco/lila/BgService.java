package eu.mvanco.lila;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.google.android.gms.internal.ca;

import eu.mvanco.camera.PhotoIntentActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * When started it will run on its own thread. It plans with Alarm Manager events when
 * the DailyChallengeReciever (start of notification) and EndDailyChallengeReceiver (end
 * of notification) will called.
 * @author Matus  
 *
 */
public class BgService extends Service
{
	
	private Handler handler;  
	private String plannedTime;
	public static String IMAGINARY_DAILY_CHALLENGE = "imaginary_daily_challenge";
	private boolean imaginaryDailyChallenge;
	
	
    //NotificationManager mNM;
    Thread thr;

    @Override
    public void onCreate() {
       // mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // show the icon in the status bar
        //showNotification();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        thr = new Thread(null, mTask, "SettingDailyChallengeService");
        
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	handler = new Handler(); 
    	//Toast.makeText(this, , Toast.LENGTH_SHORT).show();
    	
    	imaginaryDailyChallenge = intent.getBooleanExtra(IMAGINARY_DAILY_CHALLENGE, false);
    	
    	if (imaginaryDailyChallenge) {
    		getDailyChallenge();
		}
    	else {
    		thr.start();
    	}
    	
		return START_REDELIVER_INTENT;
    }
    


    @Override
    public void onDestroy() {
        // Cancel the notification -- we use the same ID that we had used to start it
        //mNM.cancel(R.string.hello_world);

        // Tell the user we stopped.
        //Toast.makeText(this, R.string.stop_alarm_service, Toast.LENGTH_SHORT).show();
    }

    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        public void run() {
            // When the alarm goes off, we want to broadcast an Intent to our
            // BroadcastReceiver.  Here we make an Intent with an explicit class
            // name to have our own receiver (which has been published in
            // AndroidManifest.xml) instantiated and called, and then create an
            // IntentSender to have the intent executed as a broadcast.
        	
        	planDailyChallenge();
            
        }
    };
    
    private void getDailyChallenge() {
        
        Calendar randomTime = getRandomTime();
        plannedTime = randomTime.get(Calendar.HOUR_OF_DAY) + ":" + randomTime.get(Calendar.MINUTE) + " ";
        Log.d("BgService", plannedTime);
        
        SharedPreferences userDetails = PreferenceManager.getDefaultSharedPreferences(this);
        Editor edit = userDetails.edit();
        edit.putString(IMAGINARY_DAILY_CHALLENGE, plannedTime).commit();
        
    }
    
    
    private void planDailyChallenge() {
    	PendingIntent startDailyChallenge = PendingIntent.getBroadcast(BgService.this,
                0, new Intent(BgService.this, DailyChallengeReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent stopDailyChallenge = PendingIntent.getBroadcast(BgService.this,
        		0, new Intent(BgService.this, EndDailyChallengeReceiver.class), 0);
        		

        // We want the alarm to go off 30 seconds from now.
        //Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        
        deleteFile();
        
        Calendar randomTime = getRandomTime();
        plannedTime = randomTime.get(Calendar.HOUR_OF_DAY) + ":" + randomTime.get(Calendar.MINUTE) + " ";
        Log.d("BgService", plannedTime);
        
        saveForAnalysis(plannedTime);
        
        
        long startTimeInMillis = randomTime.getTimeInMillis();
        int durationOfDailyChallenge = 5; // in minutes
        long stopTimeInMillis = startTimeInMillis + durationOfDailyChallenge * 1000 * 60; 

        // Schedule the alarm!
        
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(startDailyChallenge);
        
        am.set(AlarmManager.RTC, startTimeInMillis, startDailyChallenge);
        //am.set(AlarmManager.RTC, calendar.getTimeInMillis() + 30 *1000, stopDailyChallenge);

        // Done with our work...  stop the service!


        
        BgService.this.stopSelf();
    }
    
    private boolean isActivePreset(String wd) {
    	Calendar now = Calendar.getInstance();
    	now.add(Calendar.DAY_OF_MONTH, 1);
    	int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
    	
    	int[] daysOfWeek = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
    			Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY };
    	
    	//finds out for which index in weekdays string we are intrested
    	int index = -1;
    	for (int i = 0; i < 7; i++) {
    		if (dayOfWeek == daysOfWeek[i]) {
    			index = i;
    			break;
    		}
    	}

    	return (wd.charAt(index) == '1');
    }
    
    private void deleteFile() {
		File dir = getFilesDir();
		File file = new File(dir, "analysis_file");
		if (file != null)
			file.delete();
    }
    
    
    private void saveForAnalysis(String message) {
    	
		String path = BgService.this.getFilesDir().getAbsolutePath();
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("analysis_file", Context.MODE_APPEND);
			path = fos.getFD().toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		
		StringBuilder sb = new StringBuilder();
		sb.append(cal.get(Calendar.HOUR_OF_DAY));
		sb.append(":");
		sb.append(cal.get(Calendar.MINUTE));
		sb.append(" den: ");
		sb.append(cal.get(Calendar.DAY_OF_MONTH));
		sb.append("\n"); */
		
		try {
			fos.write(message.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void saveImaginaryForAnalysis(String message) {
    	
		String path = BgService.this.getFilesDir().getAbsolutePath();
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("analysis_imaginary_file", Context.MODE_APPEND);
			path = fos.getFD().toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		
		StringBuilder sb = new StringBuilder();
		sb.append(cal.get(Calendar.HOUR_OF_DAY));
		sb.append(":");
		sb.append(cal.get(Calendar.MINUTE));
		sb.append(" den: ");
		sb.append(cal.get(Calendar.DAY_OF_MONTH));
		sb.append("\n"); */
		
		try {
			fos.write(message.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /*
     * Finds out if the selected time (first parameter) is allowed as alert in this app.
     * This function compare only with one preset.
     */
    private boolean isInQuietTime(Calendar askCal, Calendar fromTime, Calendar untilTime, String weekdays) {
    	//weekday of selected calendar is get from current time
    	
    	// askCal important time
    	// week is like today
    	
    	//fromt time is important time
    	//untilTIme is important time
    	//strig weekdays all
    	
    	
    	if (isActivePreset(weekdays)) {
    		if (askCal.after(fromTime) && askCal.before(untilTime)) {
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	else {	
    		return false;
    	}
    }
    //if the ban is allowed on this preset
    private boolean isInSleepingTime(Calendar askCal, String fromTime, String untilTime) {
		Calendar from = Calendar.getInstance();
		from.add(Calendar.DAY_OF_MONTH, 1);
		from.set(Calendar.HOUR_OF_DAY, Integer.valueOf(fromTime.split(":")[0]));
		from.set(Calendar.MINUTE, Integer.valueOf(fromTime.split(":")[1]));
		
		Calendar to = Calendar.getInstance();
		to.add(Calendar.DAY_OF_MONTH, 1);
		to.set(Calendar.HOUR_OF_DAY, Integer.valueOf(untilTime.split(":")[0]));
		to.set(Calendar.MINUTE, Integer.valueOf(untilTime.split(":")[1]));
    	
    	if (from.after(to)) {
    		if (askCal.after(to) && askCal.before(from)) {
    			return false;
    		}
    		else {
    			return true;
    		}
    	}
    	else {	
    		if (askCal.after(from) && askCal.before(to)) {
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    }
    
    private long getSeed() {
    	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		
		if (!prefs.contains("seed")) {
			Long newSeed = Calendar.getInstance().getTimeInMillis();
			editor.putLong("seed", newSeed);
			editor.commit();
		}
			
		Long seed = prefs.getLong("seed", -1);
    	
    	return seed;
    }
    
    private long getRandNoOrder() {
    	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		
		long order = 0;
		if (prefs.contains("order")) {
			order = prefs.getLong("order", -1);
		}
		else {
			editor.putLong("order", order);
			editor.commit();
		}
		
    	return order;
    }
    
    private void incrementRandNoOrder() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		
		long order = 1;
		if (prefs.contains("order")) {
			order = prefs.getLong("order", -1);
		}
			
		order++; 
		
		editor.putLong("order", order);
		editor.commit();
    }
    
    private Random getRandomGenerator() {
    	
    	Random generator = new Random(getSeed());
    	
    	for (long i = getRandNoOrder(); i > 0; i--) {
    		int randomNumber = generator.nextInt(86400);
    		Log.v("rand", "returning " + String.valueOf(randomNumber));
    	}
    	
    	return generator;
    }
    
    private int getRandomNumber(Random generator) {
    	int randomNo = generator.nextInt(86400);
    	Log.v("rand", "generating " + String.valueOf(randomNo));
    	
    	incrementRandNoOrder();
    	
    	return randomNo;
    }
    
    
    /* Function return from 0 until 86400 (number of seconds in one day) randomly and
     * according to sleeping time and other presets. Test of synchronization.
     */
    private Calendar getRandomTime() {
    	
    	int randomNo = 0;
    	Calendar tryTime = Calendar.getInstance();
    	
    	Random generator = getRandomGenerator();
    	
    	boolean isCorrectTime = false;
    	while (!isCorrectTime) {
    	
	    	
	    	randomNo = getRandomNumber(generator);
	    	Log.v("rand", String.valueOf(randomNo));
	    	
	    	tryTime = convSecToTime(randomNo);
	    	tryTime.add(Calendar.DAY_OF_MONTH, 1);
	    	
	    	isCorrectTime = true;
	    	
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String fromTime = prefs.getString("alarm_time", getResources().getString(
							R.string.sleeping_time_start_default));
			String untilTime = prefs.getString("alarm_time_to", getResources().getString(
							R.string.sleeping_time_end_default));
	    	
	    	if (isInSleepingTime(tryTime, fromTime, untilTime)) {
	    		isCorrectTime = false;
	    		Log.v("rand_resp", "bad number");
	    		continue;
	    	}
	    	
	    	
	    	if (getPresets() != null) {
		    	for (Preset p : getPresets()) {
		    		if (isInQuietTime(tryTime, p.from, p.to, p.wds)) {
		    			isCorrectTime = false;
		    			Log.v("rand_resp", "bad number");
		    			break;
		    		}
		    	}
	    	}
    	}

    	return tryTime;
    }
    
    private Calendar convSecToTime(int sec) {
    	Calendar cal = Calendar.getInstance();
    	
    	int hour = sec / (60 * 60);
    	cal.set(Calendar.HOUR_OF_DAY, hour);
    	
    	sec = sec % (60 * 60);
    	int minute = sec / 60;
    	cal.set(Calendar.MINUTE, minute);

    	sec = sec % 60;
    	cal.set(Calendar.SECOND, sec);
    	
    	
    	return cal;
    }
    
    
    private List<Preset> getPresets() {
    	List<Preset> presets = new ArrayList<Preset>();
    	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String listOfPresets = prefs.getString("listOfPresets", "");
		if (listOfPresets.isEmpty())
			return null;
			
		for (String presetName : listOfPresets.split(";")) {
			String[] properties = prefs.getString(presetName, "").split(";");
			Preset newPreset = new Preset(properties[0], properties[1], properties[2]);
			presets.add(newPreset);
		}
		
    	return presets;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

