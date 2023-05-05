package eu.mvanco.lila.settings;

import android.content.Context;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public abstract class PreferenceEnabler implements OnCheckedChangeListener {

	protected final Context mContext;
	private Switch mSwitch;

	public PreferenceEnabler(Context context, Switch swtch) {
		mContext = context;
		setSwitch(swtch);
	}

	public void resume() {
		mSwitch.setOnCheckedChangeListener(this);
		mSwitch.setChecked(isSwitchOn());
	}

	public void pause() {
		mSwitch.setOnCheckedChangeListener(null);
	}

	public void setSwitch(Switch switch_) {
		if (mSwitch == switch_)
			return;
		if (mSwitch != null)
			mSwitch.setOnCheckedChangeListener(null);
		mSwitch = switch_;
		mSwitch.setOnCheckedChangeListener(this);

		mSwitch.setChecked(isSwitchOn());
	}

	public abstract boolean isSwitchOn();

}
