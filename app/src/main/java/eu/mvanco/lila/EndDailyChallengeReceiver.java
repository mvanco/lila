package eu.mvanco.lila;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This receiver is called when Daily Challenge need to be stopped. It cancel notification
 * showed by DailyChallengeReceiver.
 */
public class EndDailyChallengeReceiver extends BroadcastReceiver
{
	
	NotificationManager mNM;

	@Override
	public void onReceive(Context context, Intent intent) {
		mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.cancel(R.string.app_name);

	}

}
