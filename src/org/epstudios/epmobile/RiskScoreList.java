package org.epstudios.epmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class RiskScoreList extends EpListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.risk_score_list,
				android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CharSequence selection = ((TextView) view).getText();
				if (selection.equals(getString(R.string.chads_title)))
					chadsScore();
				else if (selection.equals(getString(R.string.chadsvasc_title)))
					chadsVascScore();
				else if (selection.equals(getString(R.string.hasbled_title)))
					hasBledScore();
			}
		});
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
