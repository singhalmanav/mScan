/*
 * @author manavsinghal
 *	MainActivity.java - This activity is first launched on the opening of application. (Home Screen)
 */

package com.manav.opswat;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	/* Declare UI Widgets */
	RelativeLayout file_upload_box, scan_result_box;
	ImageView info_icon, settings_icon;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		/* Initialize UI Widgets */
		file_upload_box = (RelativeLayout) findViewById(R.id.file_upload_container);
		scan_result_box = (RelativeLayout) findViewById(R.id.scan_data_container);
		info_icon=(ImageView) findViewById(R.id.title_icon);
		settings_icon=(ImageView) findViewById(R.id.setings_button);

		/* OnClick Handler for Settings Button */
		settings_icon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),Settings.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});

		/* About App - Info Icon Click Handler */
		info_icon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),About.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});

		/* Scan File/Folder - Click Handler */
		file_upload_box.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, FileUploader.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});

		/* Scan Reports - Click Handler */
		scan_result_box.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),RecordsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}

}
