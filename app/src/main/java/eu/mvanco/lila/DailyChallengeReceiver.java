package eu.mvanco.lila;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import eu.mvanco.camera.PhotoIntentActivity;

/**
 * This receiver serves event when the Daily Challenge is started. It shows notification
 * in notification manager. After click, user can take the picture for the purpose of app. 
 */
public class DailyChallengeReceiver extends BroadcastReceiver
{
	private Context ctx = null;
	private SharedPreferences prefs = null;
	private NotificationManager mNM;
	private Notification.Builder builder = null;
	private static boolean shouldFinish = false;
	private Intent intent = null;
	
	public static final String FINISH_DAILY_CHALLENGE = "finishDailyChallenge";

    // Start a lengthy operation in a background thread
    Thread thread = null;
    Runnable runnable =
        new Runnable() {
            @Override
            public void run() {
            	
                // In this sample, we'll use the same text for the ticker and the expanded notification
                CharSequence text = ctx.getText(R.string.start_alarm_service);
                
                Intent dailyChallengeIntent = new Intent(ctx, DailyChallengeReceiver.class);
                dailyChallengeIntent.putExtra(FINISH_DAILY_CHALLENGE, true);
                PendingIntent stopDailyChallenge = PendingIntent.getBroadcast(ctx,
                		0, dailyChallengeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                
                Intent cameraIntent = new Intent(ctx, PhotoIntentActivity.class);
                cameraIntent.putExtra(PhotoIntentActivity.START_CAMERA, true);
                
                PendingIntent camera = PendingIntent.getActivity(ctx,
                		0, cameraIntent, PendingIntent.FLAG_UPDATE_CURRENT);
              
                Calendar now = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MINUTE, 5);
                cal.set(Calendar.SECOND, 0);
                
                builder = new Notification.Builder(ctx)
                .setContentTitle(ctx.getString(R.string.daily_challenge_notification))
                .setContentText("Quickly click here to take the picture")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(camera)
                .setContentInfo(ctx.getString(R.string.daily_challenge_content))
                .addAction(R.drawable.cancel, "Dismiss", stopDailyChallenge)
                .addAction(R.drawable.ok, "Accept", camera)
                .setWhen(0)
                .setTicker("New Lila's Daily Challenge");

                
            	
            	String strRingtonePreference = null;
        		boolean enableSounds = prefs.getBoolean("enable_sounds", true);
        		boolean enableVibrations = prefs.getBoolean("enable_vibrations", true);
        		
        		
            	
                // Do the "lengthy" operation 20 times
                for (int incr = 0; incr < 60 * 5; incr++) {
                        // Sets the progress indicator to a max value, the
                        // current completion percentage, and "determinate"
                        // state
                	
                	builder.setProgress(60 * 5, incr, false);
                	
                	if (incr % 100 == 0) {
                		if (enableSounds) {
                			strRingtonePreference = prefs.getString("notification_sound", "DEFAULT_SOUND");        
                			builder.setSound(Uri.parse(strRingtonePreference));
                		}
                		
                		if (enableVibrations) {
	                		long[] vibratePattern = new long[] {400, 200, 100, 300, 2000, 200, 100, 300};
	                		Vibrator vibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
	                		vibrator.vibrate(vibratePattern, -1);
                		}
                	}
                	else {
                		builder.setSound(null);
                	}

                        // Displays the progress bar for the first time.
                	if (shouldFinish) {
                		shouldFinish = false;
                		mNM.cancel(R.string.app_name);
                		return;
                	}
                		
                	
                	int min = cal.get(Calendar.MINUTE);
                	String minutes = (min > 9) ? String.valueOf(min) : "0" + String.valueOf(min);
                	int sec = cal.get(Calendar.SECOND);
                	String seconds = (sec > 9) ? String.valueOf(sec) : "0" + String.valueOf(sec);
                	builder.setContentInfo("-" + minutes + ":" + seconds);
                	cal.add(Calendar.SECOND, -1);
                	
                	
                	mNM.notify(R.string.app_name, builder.build());
                            // Sleeps the thread, simulating an operation
                            // that takes time
                            try {
                                // Sleep for 5 seconds
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.d("DailyChallengeReceiver", "sleep failure");
                            }
                }
                mNM.cancel(R.string.app_name);
                
                
                builder = new Notification.Builder(ctx);
                // When the loop is finished, updates the notification
                
                builder
                .setContentTitle("Missed Lila's Daily Challenge")
                .setContentText("Today's Daily Challenge has been missed :(")
                .setContentIntent(stopDailyChallenge)
                .setSmallIcon(R.drawable.logo)
                .setTicker("Lila's Daily Challenge missed :(")
                .setWhen(now.getTimeInMillis());
                
                mNM.notify(R.string.app_name, builder.build());
            }
        };
	
	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent inte) {
		ctx = context;
		intent = inte;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		thread = new Thread(runnable);
		
		
		if (intent.getBooleanExtra(FINISH_DAILY_CHALLENGE, false))
			shouldFinish = true;
		else
			thread.start();
	}
}
