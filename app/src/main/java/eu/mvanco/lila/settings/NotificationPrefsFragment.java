package eu.mvanco.lila.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import eu.mvanco.lila.R;

/**
 * 
 * @author Matus
 *
 */
public class NotificationPrefsFragment extends PreferenceFragment {

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		addPreferencesFromResource(R.xml.notification);
	}
	
}
