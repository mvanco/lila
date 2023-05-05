package eu.mvanco.lila.settings;

import java.util.ArrayList;
import java.util.List;

import eu.mvanco.lila.R;

import android.preference.PreferenceActivity;
import android.widget.ListAdapter;

/**
 * This is activity of all preferences in application. It usese new framework interface for Android 3.0
 * and higher. It contains two headers, which are independent settings. In this activity is used very
 * advanced feature for showing switch bar directly near header names. It is not basic function of Android
 * and for this purpose there are classes like MyPrefsHeaderAdapter, PreferenceEnabler and SoundEnabler in
 * eu.mvanco.lila.settings package.
 * 
 * @author Matus
 *
 */
public class MyPrefsActivity extends PreferenceActivity {

	private List<Header> mHeaders;

	protected void onResume() {
		super.onResume();
		
		setTitle("Settings"); 
		
		if (getListAdapter() instanceof MyPrefsHeaderAdapter)
			((MyPrefsHeaderAdapter) getListAdapter()).resume();
		
		
	}

	protected void onPause() {
		super.onPause();
		if (getListAdapter() instanceof MyPrefsHeaderAdapter)
			((MyPrefsHeaderAdapter) getListAdapter()).pause();
	}

	public void onBuildHeaders(List<Header> target) {
		// Called when the settings screen is up for the first time
		// we load the headers from our xml description

		loadHeadersFromResource(R.xml.prefs_headers, target); 

		mHeaders = target;
	}

	public void setListAdapter(ListAdapter adapter) {
		int i, count;

		if (mHeaders == null) {
			mHeaders = new ArrayList<Header>();
			// When the saved state provides the list of headers,
			// onBuildHeaders is not called
			// so we build it from the adapter given, then use our own adapter

			count = adapter.getCount();
			for (i = 0; i < count; ++i)
				mHeaders.add((Header) adapter.getItem(i));
		}

		super.setListAdapter(new MyPrefsHeaderAdapter(this, mHeaders));
	}

}
