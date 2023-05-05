package eu.mvanco.lila;

import java.util.Calendar;

public class Preset {
	Calendar from;
	Calendar to;
	public String fromTime;
	public String toTime;
	public String wds;
	
	public Preset(String fromT, String toT, String wd) {
		fromTime = fromT;
		toTime = toT;
		wds = wd;
		from = Calendar.getInstance();
		from.add(Calendar.DAY_OF_MONTH, 1);
		
		int hourOfDay = Integer.valueOf(fromTime.split(":")[0]);
		
		
		from.set(Calendar.MINUTE, Integer.valueOf(fromTime.split(":")[1]));
		
		int am_pm = Calendar.PM;
		if (hourOfDay >= 0 && hourOfDay < 12)
			am_pm = Calendar.AM;
		// from 12 to 23 is used implicit value 'PM'
		
		int hour = hourOfDay;
		if (hourOfDay == 0)
			hour = 12;
		// from 1 to 11 will be used implicit values
		else if (hourOfDay > 12 && hourOfDay <= 23)
			hour = hourOfDay - 12;

		
		from.set(Calendar.HOUR, hour);
		from.set(Calendar.AM_PM, am_pm);
		from.set(Calendar.HOUR_OF_DAY, hourOfDay);
		
		to = Calendar.getInstance();
		to.add(Calendar.DAY_OF_MONTH, 1);
		
		hourOfDay = Integer.valueOf(toTime.split(":")[0]);
		

		to.set(Calendar.MINUTE, Integer.valueOf(toTime.split(":")[1]));
		
		am_pm = Calendar.PM;
		if (hourOfDay >= 0 && hourOfDay < 12)
			am_pm = Calendar.AM;
		// from 12 to 23 is used implicit value 'PM'
		
		hour = hourOfDay;
		if (hourOfDay == 0)
			hour = 12;
		// from 1 to 11 will be used implicit values
		else if (hourOfDay > 12 && hourOfDay <= 23)
			hour = hourOfDay - 12;

		
		to.set(Calendar.HOUR, hour);
		to.set(Calendar.AM_PM, am_pm);
		to.set(Calendar.HOUR_OF_DAY, hourOfDay);
		
	}
}
