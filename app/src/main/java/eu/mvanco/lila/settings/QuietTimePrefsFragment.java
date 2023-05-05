package eu.mvanco.lila.settings;

import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import eu.mvanco.lila.R;

/**
 * This is class for showing UI of settings. Preferences are stored manually due to advanced requirements
 * for it which the normal Preferences cant do (it is mainly unlimited number of presets in settings,
 * dynamically changing list of presets and so on). For storing preset is used 2 long strings which
 * are separated by colon. Fist "listOfPresets" is used for storing list of preset names which are active
 * in application. It is shared by all presets and there is only one such string. Each preset uses other
 * string named by its name and contains specific structure which can store data for each preset.
 * 
 * @author Matus
 *
 */
public class QuietTimePrefsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	private SoundEnabler mSoundEnabler;
	int identifier = 0;
	String multipleChoices;
	String actualPresetName;
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	PreferenceScreen globalRoot;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		editor = prefs.edit();

		prefs.registerOnSharedPreferenceChangeListener(this);
	
		setPreferenceScreen(createPreferenceHierarchy());

		Activity activity = getActivity();
		ActionBar actionbar = activity.getActionBar();
		Switch actionBarSwitch = new Switch(activity);

		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
						| Gravity.RIGHT));


		actionbar.setTitle("Presets");
		//updateValues();
		mSoundEnabler = new SoundEnabler(getActivity(), actionBarSwitch);
		updateSettings(); 

		//setPreferenceScreen(createPreferenceHierarchy());

	}
	
	private boolean isSamePreset(String findPreset) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String listOfPresets = prefs.getString("listOfPresets", "");
		String[] presets = listOfPresets.split(";");
		for (String preset : presets) {
			if (preset.compareTo(findPreset) == 0)
				return true;
		}
		
		return false;
	}
	
	private boolean addPreset(String name) {
		if (isSamePreset(name)) {
			Toast.makeText(getActivity(), "This preset has been already created.",
					Toast.LENGTH_LONG).show();
			
			
			return false;
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = prefs.edit();
		
		StringBuilder sb = new StringBuilder();
		sb.append("22:00"); //this will be also variable
		sb.append(";");
		sb.append("06:00"); //this will be also variable
		sb.append(";");
		sb.append("0000000"); //this will be also variable
		editor.putString(name, sb.toString());
		
		sb  = new StringBuilder();
		String listOfPresets = prefs.getString("listOfPresets", "");
		sb.append(listOfPresets);
		if (!listOfPresets.isEmpty())
			sb.append(";");
		sb.append(name);
		editor.putString("listOfPresets", sb.toString());
		editor.commit();
		return true;
	}
	
	/* 
	 * Takes string with 7 characters '1' or '0' and returns array of 7 booleans according this string
	 * In case of error returns null
	 */
	public static boolean[] getMultipleChoiceValues(String codedWeekdays) {
		final int ALLOWED_SIZE = 7;
		
		if (codedWeekdays.length() != ALLOWED_SIZE)
			return null;
		
		boolean[] result = new boolean[7];
		
		for (int i = 0; i < ALLOWED_SIZE; i++) {
			 result[i] = ((codedWeekdays.charAt(i) - '0') == 1) ? true : false;
		}
		
		return result;
	}
	
	/* Function which creates dialog for selecting weekdays which the quiet time presets will be inactive in. */
	protected Dialog myDialog(String wd) {
		boolean[] multipleChoices;
		if ((multipleChoices = getMultipleChoiceValues(wd)) == null)
			return null;
		
		final boolean[] newMultipleChoices = new boolean[7];
		for (int i = 0; i < 7; i++) {
			newMultipleChoices[i] = multipleChoices[i];
		}

		return new AlertDialog.Builder(getActivity())
        .setIcon(R.drawable.ic_popup_reminder)
        .setTitle("Repeat preset")
        .setMultiChoiceItems(R.array.weekdays,
        		multipleChoices,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton,
                            boolean isChecked) {
                    	
                    	//change
                    	newMultipleChoices[whichButton] = (isChecked) ? true : false;

                    	
                        /* User clicked on a check box do some stuff */
                    }
                })
        .setPositiveButton(R.string.alert_dialog_ok,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	StringBuilder sb = new StringBuilder();
            	for (int i = 0; i < 7; i++) {
            		if (newMultipleChoices[i])
            			sb.append("1");
            		else
            			sb.append("0");
            	}
            	
        		StringBuilder sb2 = new StringBuilder();
        		
        		String[] properties = prefs.getString(actualPresetName, "").split(";");
        		sb2.append(properties[0]); //this will be also variable
        		sb2.append(";");
        		sb2.append(properties[1]); //this will be also variable
        		sb2.append(";");
        		sb2.append(sb.toString()); //this will be also variable
        		//Toast.makeText(getActivity(), "tu by som mal ulozit" + sb.toString()+ sb2.toString(), Toast.LENGTH_LONG).show();
        		//String noe = actualPresetName;
            	editor.putString(actualPresetName, sb2.toString());
            	editor.commit();
            	

                /* User clicked Yes so do some stuff */
            }
        })
        .setNegativeButton(R.string.alert_dialog_cancel,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked No so do some stuff */
            }
        })
       .create();
	}
	
	private PreferenceScreen createPreferenceHierarchy()  {
		if (getActivity() == null) {
			
			return null;
		}
        PreferenceManager prefMan = getPreferenceManager();
        PreferenceScreen root = prefMan.createPreferenceScreen(getActivity());
        root.removeAll();
        
        Preference addPresetButton = new Preference(getActivity());
        addPresetButton.setTitle("Add preset");
        root.addPreference(addPresetButton);
        
        globalRoot = root;
        
        addPresetButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
           	 
           	 
           	 AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

           	 alert.setTitle("Add preset");
           	 alert.setMessage("Name:");

           	 // Set an EditText view to get user input
           	 final EditText input = new EditText(getActivity());
           	 alert.setView(input);

           	 alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
           	 public void onClick(DialogInterface dialog, int whichButton) {
               	   String value = input.getText().toString();
               	   if (!addPreset(value))
               		   Toast.makeText(getActivity(), "Dominika to sa takto nerobi :(", Toast.LENGTH_LONG).show();
               	   setPreferenceScreen(createPreferenceHierarchy());
           	   }
           	 });

           	 alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           	   public void onClick(DialogInterface dialog, int whichButton) {
           	     // Canceled- nothing to do
           	   }
           	 });

           	 alert.show();
                
                
                
                return true;
            }
        });
        
        PreferenceCategory savedPresets = new PreferenceCategory(getActivity());
        savedPresets.setTitle("Saved presets");
        root.addPreference(savedPresets);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String listOfPresets = prefs.getString("listOfPresets", "");
		
		for (String presetName : listOfPresets.split(";")) {
			
			if (presetName.isEmpty())
				break;
			
			String[] properties = prefs.getString(presetName, "").split(";");
	        
	        // add preference screen preference
	        PreferenceScreen screenPref = prefMan.createPreferenceScreen(getActivity());
	        screenPref.setKey("screen_preference_" + String.valueOf(++identifier));
	        screenPref.setTitle(presetName);
	        savedPresets.addPreference(screenPref);
	        
	        //inner preferences
	        PreferenceCategory prefCat = new PreferenceCategory(getActivity());
	        prefCat.setTitle("Sleeping time");
	        screenPref.addPreference(prefCat);
	
	        // Checkbox preference
	        QuietTimePickerPreference picker = new QuietTimePickerPreference(getActivity(), null, presetName, true);
	        picker.setKey("from_picker_" + String.valueOf(identifier));
	        picker.setTitle(R.string.sett_sleeping_from);
	        picker.setSummary(properties[0]);
	        prefCat.addPreference(picker);
	        
	        // Checkbox preference
	        QuietTimePickerPreference picker2 = new QuietTimePickerPreference(getActivity(), null, presetName, false);
	        picker2.setKey("until_picker_" + String.valueOf(identifier));
	        picker2.setTitle(R.string.sett_sleeping_to);
	        picker2.setSummary(properties[1]);
	        prefCat.addPreference(picker2);
	        
	        PreferenceCategory moreCat = new PreferenceCategory(getActivity());
	        moreCat.setTitle("More...");
	        screenPref.addPreference(moreCat);
	        
	        //in more category
	        final Preference setWeekdays = new Preference(getActivity());
	        setWeekdays.setTitle("Select weekdays");
	        Intent i = new Intent();
	        i.putExtra("eu.mvanco.lila.presetName", presetName);
	        setWeekdays.setIntent(i);
	        moreCat.addPreference(setWeekdays);
	        
	        setWeekdays.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	                     public boolean onPreferenceClick(Preference preference) {
	                    	 Intent i = setWeekdays.getIntent();
	                    	 actualPresetName = i.getStringExtra("eu.mvanco.lila.presetName");
	                    	 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	                    	 String[] str = prefs.getString(actualPresetName, "00:00;00:00:0000000").split(";");
	                    	 myDialog(str[2]).show();
	                         return true;
	                     }
	                 });
	        
	        final Preference deleteButton = new Preference(getActivity());
	        deleteButton.setTitle("Delete preset");
	        Intent in = new Intent();
	        in.putExtra("eu.mvanco.lila.deletePresetName", presetName);
	        deleteButton.setIntent(in);
	        
	        
	        deleteButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
					Intent i = deleteButton.getIntent();
					actualPresetName = i.getStringExtra("eu.mvanco.lila.deletePresetName");
					
			        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			        builder.setMessage("Do you want to really delete selected preset?")
			               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                	   	deletePreset(actualPresetName);
			                	   
				   					Intent goBack = new Intent(getActivity(), MyPrefsActivity.class );
									goBack.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, QuietTimePrefsFragment.class.getName() );
									startActivity(goBack);
			                   }
			               })
			               .setNegativeButton("No", new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                       //noting, delete all method
			                   }
			               });
			        builder.show();					
                    return true;
                }
               
				private void deletePreset(String actualPresetName) {					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
					String listOfPresets = prefs.getString("listOfPresets", "");
					int position = 0;
					String[] presets = listOfPresets.split(";");
					for (String preset : presets) {
						if (preset.contentEquals(actualPresetName))
							break;
						else
							position++;
					}
					
					// ak pozicia bude rovna poctu strednikov v retazci listOfPresets tak zrusi
					List<String> items = Arrays.asList(presets);
					if (position == items.size()) {
						return;
					}
					
					// from inicializuje na 0 a ak je position >=1 tak nastavi poziciu na prislusny strednik
					int from = 1;
					while (position >= 1) {
						from = listOfPresets.indexOf(";", from) + 1;
						
						
						position--;
					}
					
					if (from == 1) {
						from = 0;
					}
					
					//until nastavi na dalsi strednik, alebo znak za retazcom - toto by malo vymazat vsetko PO tento znak,
					//teda mimo tohto znaku
					int until = listOfPresets.indexOf(";", from);
					if (until == -1) {
						until = listOfPresets.length();
						if (from > 0)
							from--;
					}
					else {
						until++; //to remove also ;
					}
					
					//uz iba vymazat na zaklade pozicii
					StringBuffer sb = new StringBuffer(listOfPresets);
					StringBuffer afterRemoval = sb.delete(from, until);
					editor.putString("listOfPresets", afterRemoval.toString());
					editor.remove(actualPresetName);
					editor.commit();
					
					
				}
            });
	        
	        moreCat.addPreference(deleteButton);
	        
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.commit();
		}
		
		return root;
	}
	
	protected static String getWeekdays(Context ctx, String presetName) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String[] str = prefs.getString(presetName, "00:00;00:00:0000000").split(";");
		
		return str[2];
	}
	
	/*
	private void buildMyCustomLayout(String presetName) {
		//begin
        
        
        PreferenceManager prefman = (PreferenceManager) getPreferenceManager();
        PreferenceCategory root = (PreferenceCategory) prefman.findPreference("saved_pres");
   	 
        
        // add preference screen preference
        PreferenceScreen screenPref = prefman.createPreferenceScreen(getActivity());
        screenPref.setKey("screen_preference_" + String.valueOf(++identifier));
        screenPref.setTitle(presetName);
        root.addPreference(screenPref);
        
        PreferenceCategory prefCat = new PreferenceCategory(getActivity());
        prefCat.setTitle("Sleeping time");
        screenPref.addPreference(prefCat);

        // Checkbox preference
        TimePickerPreference picker = new TimePickerPreference(getActivity(), null);
        picker.setKey("picker_" + String.valueOf(identifier));
        picker.setTitle(R.string.sett_sleeping_from);
        prefCat.addPreference(picker);
        
        // Checkbox preference
        TimePickerPreference picker2 = new TimePickerPreference(getActivity(), null);
        picker2.setKey("picker_" + String.valueOf(identifier));
        picker2.setTitle(R.string.sett_sleeping_to);
        prefCat.addPreference(picker2);
        
        PreferenceCategory moreCat = new PreferenceCategory(getActivity());
        moreCat.setTitle("More...");
        screenPref.addPreference(moreCat);
        
        
        Preference setWeekdays = new Preference(getActivity());
        setWeekdays.setTitle("Select weekdays");
        moreCat.addPreference(setWeekdays);
        
        multipleChoice - 
        setWeekdays.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                     public boolean onPreferenceClick(Preference preference) {
                    	 myDialog().show();
                         
                         
                         
                         return true;
                     }
                 });
        
        Preference deleteButton = new Preference(getActivity());
        deleteButton.setTitle("Delete preset");
        moreCat.addPreference(deleteButton);
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.commit();
	} */

	public void onResume() {
		super.onResume();
		mSoundEnabler.resume();
		updateSettings();
		//updateValues();
		setPreferenceScreen(createPreferenceHierarchy());
	}

	public void onPause() {
		super.onPause();
		mSoundEnabler.pause();
	}

	protected void updateSettings() {

		boolean available = mSoundEnabler.isSwitchOn();
		if (getPreferenceScreen() != null) {
		int count = getPreferenceScreen().getPreferenceCount();
		for (int i = 0; i < count; ++i) {
			Preference pref = getPreferenceScreen().getPreference(i);
			pref.setEnabled(available);
		}
		
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("SOUND_ENABLED")) {
			updateSettings();
			//Toast.makeText(getActivity(), "teraz sa meni", Toast.LENGTH_SHORT).show();
			
			updateValues();
		}
		else {
			setPreferenceScreen(createPreferenceHierarchy());
		}
	}
	
	private void updateValues() {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		//String listOfPresets = prefs.getString("listOfPresets", "");
		
		/*
		for (int i = 0; i < 999; i++) {
			QuietTimePickerPreference picker = (QuietTimePickerPreference) findPreference("from_picker_" + i);
			QuietTimePickerPreference picker2 = (QuietTimePickerPreference) findPreference("until_picker_" + i);
			picker.setSummary(properties[0]);
			picker2.setSummary(properties[1]);
		} */
		/*
		for (String presetName : listOfPresets.split(";")) {
			
			if (presetName.isEmpty())
				break;
			
			String[] properties = prefs.getString(presetName, "").split(";");
	        // Checkbox preference
	        QuietTimePickerPreference picker = new QuietTimePickerPreference(getActivity(), null, presetName, true);
	        picker.setSummary(properties[0]);
	        prefCat.addPreference(picker);
	        
	        // Checkbox preference
	        QuietTimePickerPreference picker2 = new QuietTimePickerPreference(getActivity(), null, presetName, false);
	        picker2.setSummary(properties[1]);
			
		} */
		/*
		
		if (mSoundEnabler != null && mSoundEnabler.isSwitchOn()) {
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
		}  */
	} 
}
