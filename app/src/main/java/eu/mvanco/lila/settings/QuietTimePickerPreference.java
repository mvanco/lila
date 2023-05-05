package eu.mvanco.lila.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * This is extention of basic Preference class where the user can pick the time for purpose of app
 * and this time will by after close stored forever using SharedPreferences class (which is used by
 * default by PreferenceFragment or PreferenceActivity as well).
 * 
 * @author Matus
 *
 */
public class QuietTimePickerPreference extends DialogPreference {
	String presetName;
	boolean isFromTime;
	Context context;
	
    public QuietTimePickerPreference(Context ctx, AttributeSet attrs, String pn, boolean ih) {
		super(ctx, attrs);

		setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
        //this.set
        presetName = pn;
        isFromTime = ih;
        context = ctx;
	}

	private int lastHour=0;
    private int lastMinute=0;
    private TimePicker picker=null;

    public static int getHour(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[1]));
    }
    
    public String getTime() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String[] properties = prefs.getString(presetName, "").split(";");
		String newTime;
		if (isFromTime) {
			newTime = properties[0];
		}
		else
			newTime = properties[1];
		return newTime;
    }


    @Override
    protected View onCreateDialogView() {
        picker=new TimePicker(getContext());

        return(picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        
        String time = getTime();
        
        lastHour = getHour(time);
        lastMinute = getMinute(time);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
/*
        if (positiveResult) {
            lastHour=picker.getCurrentHour();
            lastMinute=picker.getCurrentMinute();

            String time=String.format("%02d", lastHour)+":"+String.format("%02d", lastMinute);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }*/
        
        if (positiveResult) {
        	//String name = getTitle().toString();
       
        	
        	
	        lastHour=picker.getCurrentHour();
	        lastMinute=picker.getCurrentMinute();
	        
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			String[] properties = prefs.getString(presetName, "").split(";");
			
			StringBuilder sb = new StringBuilder();
			
			if (isFromTime) {
				sb.append(String.format("%02d", picker.getCurrentHour())); //this will be also variable
				sb.append(":");
				sb.append(String.format("%02d", picker.getCurrentMinute())); //this will be also variable
				sb.append(";");
				sb.append(properties[1]); //this will be also variable
				sb.append(";");
				sb.append(QuietTimePrefsFragment.getWeekdays(getContext(), presetName)); //this will be also variabl;
			}
			else {
				sb.append(properties[0]); //this will be also variable
				sb.append(";");
				sb.append(String.format("%02d", picker.getCurrentHour())); //this will be also variable
				sb.append(":");
				sb.append(String.format("%02d", picker.getCurrentMinute())); //this will be also variable
				sb.append(";");
				sb.append(QuietTimePrefsFragment.getWeekdays(getContext(), presetName)); //this will be also variable
			}
			editor.putString(presetName, sb.toString());
			
			editor.commit();
			
			setSummary(getTime());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time=null;
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String[] properties = prefs.getString(presetName, "").split(";");
		String newTime;
		if (isFromTime) {
			newTime = properties[0];
		}
		else
			newTime = properties[1];

        if (restoreValue) {
            if (defaultValue==null) {
                time= newTime;
            }
            else {
                time = newTime;
            }
        }
        else {
            time = "00:00";
        }

        lastHour=getHour(time);
        lastMinute=getMinute(time);
    }
}