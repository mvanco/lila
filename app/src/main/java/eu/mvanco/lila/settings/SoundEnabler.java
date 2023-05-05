package eu.mvanco.lila.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class SoundEnabler implements OnCheckedChangeListener {

	protected final Context mContext;
	private Switch mSwitch;

	public SoundEnabler(Context context, Switch swtch) {
		mContext = context;
		setSwitch(swtch);
	}

	public void setSwitch(Switch swtch) {
		if (mSwitch == swtch)
			return;

		if (mSwitch != null)
			mSwitch.setOnCheckedChangeListener(null);
		mSwitch = swtch;
		mSwitch.setOnCheckedChangeListener(this);

		mSwitch.setChecked(isSwitchOn());
	}

	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		SharedPreferences prefs;
		Editor editor;

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		editor.putBoolean("SOUND_ENABLED", isChecked);
		editor.commit();

	}

	public boolean isSwitchOn() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		return prefs.getBoolean("SOUND_ENABLED", true);
	}

	public void resume() {
		mSwitch.setOnCheckedChangeListener(this);
		mSwitch.setChecked(isSwitchOn());
	}

	public void pause() {
		mSwitch.setOnCheckedChangeListener(null);
	}
}
