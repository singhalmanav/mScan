/*
 * @author manavsinghal
 *	Settings.java - This activity is for the user to enter API Key and store it in Mobile Database.
 */

package com.manav.opswat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {

	// UI Widgets Declaration 
	TextView settings_label, api_key_label;
	Button enter;
	String settings_value, API_KEY_VALUE;

	/* Shared Preferences Initialization */
	SharedPreferences pref; // 0 - for private mode
	Editor editor;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		/* Initialization of UI Widgets */
		settings_label = (TextView) findViewById(R.id.settings_msg);
		api_key_label= (TextView) findViewById(R.id.api_key);
		enter= (Button) findViewById(R.id.enter);

		// Shared Preferences retrieving value 
		pref = getApplicationContext().getSharedPreferences("api_pref", MODE_PRIVATE);
		editor = pref.edit();

		settings_value= "This app requires free Metascan Online API key, please log into the OPSWAT Portal at https://portal.opswat.com, "+
				"navigate to the Security Applications tab, and go to the Metascan Online API page where you will be able to activate "+
				"your Metascan Online API key for free. \n\nFree API keys obtained through the OPSWAT Portal allow 10 file "+
				"scans and 100 hash lookups per hour. To extend your key to allow additional usage, "+
				"please contact OPSWAT Sales at http://www.opswat.com/about/contact-us";

		// Settings text for Label 
		settings_label.setText(settings_value);
		Linkify.addLinks(settings_label, Linkify.ALL);

		// IF API Key is not set before
		if(pref.getString("Api_Key", null) == null){
			api_key_label.setText("No API Key Exist");
			api_key_label.setTextColor(Color.RED);
		}
		// If API Key set then show it in label to the user.
		else {
			String api = pref.getString("Api_Key", null);
			String first_chars_API = api.substring(0, api.length()-4);
			first_chars_API= first_chars_API.replaceAll(".", "x");
			String last_chars_API = api.substring(api.length()-4, api.length());
			api_key_label.setText("API Key- "+first_chars_API+last_chars_API);
		}

		/* OnClick Listener for Enter Button */
		enter.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				promptDialogBox();
			}
		});
	}
	
	public void onBackPressed() {
	    startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	// Custom Prompt Box to the user for entering API Key
	protected void promptDialogBox() {
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(Settings.this);
		View promptsView = li.inflate(R.layout.prompt_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		// EditBox for the user to enter API Key 
		final EditText userInput = (EditText) promptsView.findViewById(R.id.api_key_editbox);

		// set dialog message
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				/* Checking User Input against NULL values */
				if (userInput.getText().toString().trim() != null){
					API_KEY_VALUE= userInput.getText().toString().trim();
					editor.putString("Api_Key", API_KEY_VALUE);
					editor.commit();

					String first_chars_API = API_KEY_VALUE.substring(0, API_KEY_VALUE.length()-4);
					first_chars_API= first_chars_API.replaceAll(".", "x");
					String last_chars_API = API_KEY_VALUE.substring(API_KEY_VALUE.length()-4, API_KEY_VALUE.length());
					api_key_label.setText("API Key- "+first_chars_API+last_chars_API);
				}
				else{
					Toast.makeText(getBaseContext(), "Please enter API Key.", Toast.LENGTH_LONG).show();
				}

			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

}
