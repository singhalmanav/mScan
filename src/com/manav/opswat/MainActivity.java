/*
 * @author manavsinghal
 *	MainActivity.java - This activity is first launched on the opening of application. (Home Screen)
 */

package com.manav.opswat;

import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/* Declare UI Widgets */
	RelativeLayout file_upload_box, scan_result_box, scan_system_box;
	ImageView info_icon, settings_icon;
	TextView scan_system_title, scan_system_desc;

	/* Declaring Shared Preferences */
	SharedPreferences pref; // 0 - for private mode
	Editor editor;
	String API_KEY;
	
	/* Boolean Flag for checking System_Scan_Service running in Background */
	boolean serviceRunningFlag = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		/* Initializing Shared Preferences */
		pref = getApplicationContext().getSharedPreferences("api_pref", MODE_PRIVATE);
		editor = pref.edit();
		API_KEY = pref.getString("Api_Key", null);

		/* Initialize UI Widgets */
		file_upload_box = (RelativeLayout) findViewById(R.id.file_upload_container);
		scan_result_box = (RelativeLayout) findViewById(R.id.scan_data_container);
		scan_system_box = (RelativeLayout) findViewById(R.id.scan_system_container);
		info_icon=(ImageView) findViewById(R.id.title_icon);
		settings_icon=(ImageView) findViewById(R.id.setings_button);
		scan_system_title = (TextView) findViewById(R.id.scan_system_heading);
		scan_system_desc = (TextView) findViewById(R.id.scan_system_description);

		/* Conditional Check for whether system scan service is running in background or not */
		if (isMyServiceRunning()){
			/* If Yes, Set Flag to True and change button text */
			serviceRunningFlag = true;
			scan_system_title.setText("Stop System Scan");
			scan_system_desc.setText("System scan in Progress ....");
			scan_system_desc.setTextColor(Color.RED);
		}

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

		/* Scan System - Click Handler */
		scan_system_box.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/* Check for Running Service */
				if (serviceRunningFlag == true){
					stopService(new Intent(MainActivity.this, SystemScanService.class));
					scan_system_title.setText("Scan System");
					scan_system_desc.setText("Scan the whole system");
					scan_system_desc.setTextColor(Color.BLACK);
					serviceRunningFlag = false;
				}
				/* If Service not Running, then display Custom alert box for users click for system scan */
				else{

					/* Initialization of Alert Dialog Box */
					final Dialog dialog = new Dialog(getBaseContext());
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.custom_dialog);

					// Set the Custom Dialog Components - text, image and button
					TextView heading = (TextView) dialog.findViewById(R.id.heading);
					heading.setText("Are you sure to scan the whole system ?");
					TextView desc = (TextView) dialog.findViewById(R.id.description);
					desc.setText(Html.fromHtml("This process is network intensive. It is preferable"+
							" that you scan your system when you are connected to Wi-Fi and have enough battery.<br><br>"+
							"The system scan will run in background as service."));

					Button okButton = (Button) dialog.findViewById(R.id.ButtonOK);
					okButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();

							/* Checking Internet connection, If available then only do HTTP call */
							ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
							if(cd.isConnectingToInternet() && API_KEY != null){
								startService(new Intent(MainActivity.this, SystemScanService.class));
								scan_system_title.setText("Stop System Scan");
								scan_system_desc.setText("System scan in Progress ....");
								scan_system_desc.setTextColor(Color.RED);
								serviceRunningFlag = true;
							}
							/* If Internet not connected */
							else if (cd.isConnectingToInternet() != true){
								Toast.makeText(getBaseContext(), "Please check the internet connection.", Toast.LENGTH_LONG).show();	
							}
							/* If API Key is NULL or Not Set */
							else if (API_KEY == null){
								Toast.makeText(getBaseContext(), "API Key Not Set", Toast.LENGTH_LONG).show();
							}
						}
					});

					/* Cancel Button for Custom Alert Dialog Box */
					Button cancelButton = (Button) dialog.findViewById(R.id.ButtonCancel);
					cancelButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});

					/* Display Alert Box */
					dialog.show();
					Window window = dialog.getWindow();
					window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				}
			}
		});
	}

	/* Method to check System Scan Running Service 
	   returns true if service running otherwise false */
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (SystemScanService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
