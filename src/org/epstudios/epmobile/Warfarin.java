/*  EP Mobile -- Mobile tools for electrophysiologists
    Copyright (C) 2011 EP Studios, Inc.
    www.epstudiossoftware.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */   

package org.epstudios.epmobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;

public class Warfarin extends EpActivity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warfarin);
		
		View calculateDoseButton = findViewById(R.id.calculate_dose_button);
		calculateDoseButton.setOnClickListener(this);
		View clearButton = findViewById(R.id.clear_button);
		clearButton.setOnClickListener(this);
		
		tabletRadioGroup = (RadioGroup) findViewById(R.id.tabletRadioGroup);
		inrTargetRadioGroup = (RadioGroup) findViewById(R.id.inrTargetRadioGroup);
		inrEditText = (EditText) findViewById(R.id.inrEditText);
		weeklyDoseEditText = (EditText) findViewById(R.id.weeklyDoseEditText);
		
		doseChange = new DoseChange(0, 0, "", Direction.INCREASE);
		
		clearEntries();
	}
	
	private RadioGroup tabletRadioGroup;
	private RadioGroup inrTargetRadioGroup;
	private EditText inrEditText;
	private EditText weeklyDoseEditText;
	private String defaultWarfarinTablet;
	private String defaultInrTarget;
	private double lowRange, highRange;
	
	private enum TargetRange { LOW_RANGE, HIGH_RANGE }
	private enum Direction { INCREASE, DECREASE }
	
	private class DoseChange {
		public DoseChange(int lowEnd, int highEnd, String message,
				Direction direction) {
			this.lowEnd = lowEnd;
			this.highEnd = highEnd;
			this.message = message;
			this.direction = direction;
		}

		private int lowEnd;
		private int highEnd;
		private String message;
		private Direction direction;
	}
	
	public DoseChange doseChange;
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.calculate_dose_button:
			calculateResult();
			break;
		case R.id.clear_button:
			clearEntries();
			break;
		}
	}
	
	public static double getNewDoseFromPercentage(double percent, double oldDose, Boolean increase) {
		return Math.round(oldDose + (increase ? oldDose * percent : -oldDose * percent));
	}
	
	private double getTabletDose() {
		double dose = 5.0;
		int id = tabletRadioGroup.getCheckedRadioButtonId();
		switch (id) {
		case R.id.tablet1:
			dose = 2.0;
			break;
		case R.id.tablet2:
			dose = 2.5;
			break;
		case R.id.tablet3:
			dose = 5.0;
			break;
		case R.id.tablet4:
			dose = 7.5;
			break;
		}
		return dose;
	}
	
	private double getWeeklyDose() {
		try {
			double weeklyDose = Double.parseDouble(weeklyDoseEditText.getText().toString());
			return weeklyDose;
		}
		catch (NumberFormatException e) {
			return 0.0;
		}
		
	}
	
	public static Boolean weeklyDoseIsSane(double dose, double tabletSize) {
		// need to make sure not only dose is sane, but max change to dose is sane
		return dose - 0.2 * dose >= 7 * 0.5 * tabletSize && dose + 0.2 * dose <= 7 * 1.5 * tabletSize;
	}
	
	private void calculateResult() {
		String message = "";
		Boolean showDoses = false;
		try {
			getRange();
			double inr = Double.parseDouble(inrEditText.getText().toString());
			if (inr >= 6.0)
				message = "Hold warfarin until INR back in therapeutic range.";
			else if (inrTherapeutic(inr))
				message = "INR is therapeutic.  No change in warfarin dose.";
			else {
				doseChange = percentDoseChange(inr);
				if (doseChange.lowEnd == 0 || doseChange.highEnd == 0)
					message = "Invalid Entries!";
				else {
					if (doseChange.message != null)
						message = doseChange.message + "\n";
					if (doseChange.direction == Direction.INCREASE)
						message = message + "Increase ";
					else
						message = message + "Decrease ";
					message = message + 
						"weekly dose by " + String.valueOf(doseChange.lowEnd) +
						"% to " + String.valueOf(doseChange.highEnd) + "%.";
					if (weeklyDoseIsSane(getWeeklyDose(), getTabletDose()))
						showDoses = true;
				}
			}
			displayResult(message, showDoses);
		}
		catch (NumberFormatException e) {
			message = "Invalid Entry";
			displayResult(message, false);
		}
	}
	
	private Boolean inrTherapeutic(double inr) {
		return lowRange <= inr && inr <= highRange;
	}
	
	private void getRange() {
		// assumes only 2 ranges
		TargetRange range = getTargetRange();
		if (range == TargetRange.LOW_RANGE) {
			lowRange = 2.0;
			highRange = 3.0;
		}
		else {			// TargetRange.HIGH_RANGE
			lowRange = 2.5;
			highRange = 3.5;
		}
	}
	
	private TargetRange getTargetRange() {
		if (inrTargetRadioGroup.getCheckedRadioButtonId() == R.id.inrTarget1)
			return TargetRange.LOW_RANGE;
		else if (inrTargetRadioGroup.getCheckedRadioButtonId() == R.id.inrTarget2)
			return TargetRange.HIGH_RANGE;
		else
			return TargetRange.LOW_RANGE;
	}
	
	private DoseChange percentDoseChange(double inr) {
		// uses Horton et al. Am Fam Physician 1999 algorithm,
		// modified to specify specific % based on subdividing inr into ranges
		TargetRange range = getTargetRange();
		if (range == TargetRange.LOW_RANGE)
			return percentDoseChangeLowRange(inr);
		else if (range == TargetRange.HIGH_RANGE)
			return percentDoseChangeHighRange(inr);
		else
			return new DoseChange(0, 0, "", Direction.INCREASE);	// error!
	}
	

	private DoseChange percentDoseChangeHighRange(double inr) {
		DoseChange doseChange = new DoseChange(0, 0, "", Direction.INCREASE);
		if (inr < 2.0) {
			doseChange.lowEnd = 10;
			doseChange.highEnd = 20;
			doseChange.message = "Give additional dose.";
		}
		else if (inr >= 2.0 && inr < 2.5) {
			doseChange.lowEnd = 5;
			doseChange.highEnd = 15;
			doseChange.direction = Direction.INCREASE;
		}
		else if (inr > 3.5 && inr < 4.6) {
			doseChange.lowEnd = 5;
			doseChange.highEnd = 15;
			doseChange.direction = Direction.DECREASE;
		}
		else if (inr >= 4.6 && inr < 5.2) {
			doseChange.lowEnd = 10;
			doseChange.highEnd = 20;
			doseChange.message = "Withhold no dose or one dose.";
			doseChange.direction = Direction.DECREASE;
		}		
		else if (inr > 5.2) {
			doseChange.lowEnd = 10;
			doseChange.highEnd = 20;
			doseChange.message = "Withhold no dose to two doses.";
			doseChange.direction = Direction.DECREASE;
		}
		
		return doseChange;
	}

	private DoseChange percentDoseChangeLowRange(double inr) {
		DoseChange doseChange = new DoseChange(0, 0, "", Direction.INCREASE);
		if (inr < 2.0) {
			doseChange.lowEnd = 5;
			doseChange.highEnd = 20;
		}
		else if (inr >= 3.0 && inr < 3.6) {
			doseChange.lowEnd = 5;
			doseChange.highEnd = 15;
			doseChange.direction = Direction.DECREASE;
		}
		else if (inr >= 3.6 && inr <= 4) {
			doseChange.lowEnd = 10;
			doseChange.highEnd = 15;
			doseChange.message = "Withhold no dose or one dose.";
			doseChange.direction = Direction.DECREASE;
		}
		else if (inr > 4) {
			doseChange.lowEnd = 10;
			doseChange.highEnd = 20;
			doseChange.message = "Withhold no dose or one dose.";
			doseChange.direction = Direction.DECREASE;
		}
		return doseChange;
	}

	private void displayResult(String message, Boolean showDoses) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		
		dialog.setMessage(message);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Reset",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearEntries();
					}
				});
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Don't Reset",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
		if (showDoses) {
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Dosing", 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						displayDoses();
					}
				});
		}
		dialog.show();
	}
	
	private void displayDoses() {
		saveDoseChange();
		Intent i = new Intent(this, DoseTable.class);
		startActivity(i);
	}
	
	private void saveDoseChange() {
		// save in Preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Editor editor = prefs.edit();
		editor.putInt("lowEnd", doseChange.lowEnd);
		editor.putInt("highEnd", doseChange.highEnd);
		editor.putBoolean("increase", doseChange.direction == Direction.INCREASE);
		editor.putFloat("tabletDose", (float) getTabletDose());
		editor.putFloat("weeklyDose", (float) getWeeklyDose());
		editor.commit();
	}
	
	private void clearEntries() {
		inrEditText.setText(null);
		weeklyDoseEditText.setText(null);
		getPrefs();
		int defaultId = Integer.parseInt(defaultWarfarinTablet);
		int id = 2;  // 5 mg default default
		switch (defaultId) {
		case 0:
			id = R.id.tablet1;
			break;
		case 1:
			id = R.id.tablet2;
			break;
		case 2:
			id = R.id.tablet3;
			break;
		case 3:
			id = R.id.tablet4;
			break;
		}
		tabletRadioGroup.check(id);
		defaultId = Integer.parseInt(defaultInrTarget);
		id = 0;
		switch (defaultId) {
		case 0:
			id = R.id.inrTarget1;
			break;
		case 1:
			id = R.id.inrTarget2;
			break;
		}
		inrTargetRadioGroup.check(id);
	}
	
	private void getPrefs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		defaultWarfarinTablet = prefs.getString("default_warfarin_tablet", "2");	// 2 = 5 mg dose
		defaultInrTarget = prefs.getString("default_inr_target", "0"); 	// 0 = 2.0 - 3.0 target
	}

}