/*
 * @author manavsinghal
 *	About.java - For showing the About App screen (Info Screen)
 */

package com.manav.opswat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class About extends Activity 
{
	/* Declare UI Widgets */
	TextView title, first_label, second_label;
	ImageView image_icon, settings_button;
	LinearLayout adlayout;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		/* Initialize Widgets Method Call */
		initialize();

		String first_data="<b><font color=#000099>mScan</font></b> application allows you to harness the power of our "+
							"cloud-based multi-scanning technology integrated directly into your mobile.";
		first_label.setText(Html.fromHtml(first_data));

		String second_data="No single anti-malware engine is perfect 100% of the time. Using multiple engines to scan for threats "+
				"allows you to take advantage of the strengths of each individual engine and to guarantee the earliest possible detection."+
				"<br/><br/>This app optimizes the included engines to enable fast, simultaneous scanning and eliminates"+
				" the hassle of licensing multiple products.";

		second_data=second_data+"<br/><br/><b><font color=#000099>Developed by Manav Singhal, University of Missouri, Columbia</font></b>";

		/* Set Text for TextView */
		second_label.setText(Html.fromHtml(second_data));

		/* OnClick Handler for Settings Button */
		settings_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),Settings.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}

	/* Initialize Widgets Method Body Defined */
	private void initialize() 
	{
		title=(TextView) findViewById(R.id.title);
		first_label=(TextView) findViewById(R.id.first_label);
		second_label=(TextView) findViewById(R.id.second_label);
		image_icon=(ImageView) findViewById(R.id.image);
		settings_button = (ImageView) findViewById(R.id.setings_button);
	}

}
