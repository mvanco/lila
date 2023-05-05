package eu.mvanco.lila.settings;

import eu.mvanco.lila.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


/**
 * This is class for showing UI of settings. Preferences are stored automatically, so it is simple
 * Preference class from XML sleeping_time_prefs.
 * 
 * @author Matus
 *
 */
public class SleepingTimePrefsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	SleepingTimePickerPreference picker;
	SleepingTimePickerPreference picker2;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.registerOnSharedPreferenceChangeListener(this);

		addPreferencesFromResource(R.xml.sleeping_time_prefs);

		Activity activity = getActivity();
		ActionBar actionbar = activity.getActionBar();



		picker = (SleepingTimePickerPreference) findPreference("alarm_time");
		picker2 = (SleepingTimePickerPreference) findPreference("alarm_time_to");

		actionbar.setTitle("Sleeping time");
		updateValues();


	}

	public void onResume() {
		super.onResume();

		updateValues();
	}

	public void onPause() {
		super.onPause();

	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updateValues();
	}

	private void updateValues() {
		if (getActivity() != null
				&& PreferenceManager
						.getDefaultSharedPreferences(getActivity()) != null) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			String option = prefs.getString(
					"alarm_time",
					getResources().getString(
							R.string.sleeping_time_start_default));
			picker.setSummary(option);
			String option2 = prefs.getString(
					"alarm_time_to",
					getResources().getString(
							R.string.sleeping_time_end_default));
			picker2.setSummary(option2);
		}
	}
}
