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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class EpMobile extends EpListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.main_index, android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CharSequence selection = ((TextView) view).getText();
				if (selection.equals("About"))
					about();
				else if (selection.equals("Cycle Length Calculator"))
					intervalRateCalculator();
				else if (selection.equals("Dofetilide Calculator"))
					dofetilideCalculator();
				else if (selection.equals("QTc Calculator"))
					qtcCalculator();
				else if (selection.equals("HAS-BLED Score"))
					hasBledScore();
				else if (selection.equals("CHADS\u2082 Score"))
					chadsScore();
				else if (selection.equals(getString(R.string.chadsvasc_title)))
					chadsVascScore();
			}
		});
	}
    
    
    private void about() {
   		Intent i = new Intent(this, About.class);
		startActivity(i);
    }
    
    private void dofetilideCalculator() {
    	Intent i = new Intent(this, Dofetilide.class);
    	startActivity(i);
    }
    
    private void qtcCalculator() {
    	Intent i = new Intent(this, Qtc.class);
    	startActivity(i);
    }
    
    private void intervalRateCalculator() {
    	Intent i = new Intent(this, CycleLength.class);
    	startActivity(i);
    }
    
    private void hasBledScore() {
    	Intent i = new Intent(this, HasBled.class);
    	startActivity(i);
    }
    
    private void chadsScore() {
       	Intent i = new Intent(this, Chads.class);
    	startActivity(i);
    }
    
    private void chadsVascScore() {
       	Intent i = new Intent(this, ChadsVasc.class);
    	startActivity(i);
    }
    
}