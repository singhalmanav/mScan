/*
 * @author manavsinghal
 *	ReportInfoActivity.java - This class represents the scan information (brief) of the scan record selected by the user.
 */

package com.manav.opswat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ReportInfoActivity extends Activity {

	/* Declare UI Widgets */
	TextView report_info_label;
	ImageView icon_image, settings_button;
	Button moreDetailsButton;

	/* Declare REST API URL and API_KEY */
	private String URL = "https://scan.metascan-online.com/v2/file";

	JSONObject data_obj;
	int total_time, percentage, total_avs;
	//String file_id;
	String start_time, scan_all_result_a, file_id, info_to_display, output= null, data_id;
	int status_code = 200;
	
	/* Declare Shared Preferences for storing API Key */ 
	SharedPreferences pref; // 0 - for private mode
	Editor editor;
	String API_KEY;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_info);

		/* Initializing Shared Preferences and retrieving API Key */
		pref = getApplicationContext().getSharedPreferences("api_pref", MODE_PRIVATE);
		editor = pref.edit();
		API_KEY = pref.getString("Api_Key", null);
		
		/* Retrieving data from previous activity using Bundle */
		Bundle extra=this.getIntent().getExtras();
		data_id = extra.getString("data_id");

		/* Initialization of UI Widgets */
		moreDetailsButton= (Button) findViewById(R.id.nextButton);
		report_info_label = (TextView) findViewById(R.id.report_label);
		icon_image = (ImageView) findViewById(R.id.icon);
		icon_image.setVisibility(View.INVISIBLE);
		settings_button = (ImageView) findViewById(R.id.setings_button);

		/* OnClick Handler for Settings Button */
		settings_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),Settings.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});

		/* HTTP Method Call for retrieving scan information pertaining to data_id */
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		if(cd.isConnectingToInternet() && API_KEY != null){
			/* Making Http Call to upload file */
			httpUploadCaller httpUpload = new httpUploadCaller(data_id);
			httpUpload.execute();
		}
		/* If Not Connected to Internet */
		else if (cd.isConnectingToInternet() != true){
			Toast.makeText(getBaseContext(), "Please check the internet connection.", Toast.LENGTH_LONG).show();
			report_info_label.setText("Please check the internet connection.");
			report_info_label.setGravity(Gravity.CENTER_HORIZONTAL);
			moreDetailsButton.setVisibility(View.INVISIBLE);
		}
		/* If API Key is NULL or not set */
		else if (API_KEY == null){
			Toast.makeText(getBaseContext(), "API Key Not Set", Toast.LENGTH_LONG).show();
			report_info_label.setText("API Key Not Set");
			report_info_label.setGravity(Gravity.CENTER_HORIZONTAL);
			moreDetailsButton.setVisibility(View.INVISIBLE);
		}

		/* Onclick Handler for 'More Details' button */
		moreDetailsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle extra=new Bundle();
				Intent intent=new Intent(getBaseContext(),ReportDetailedInfo.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				extra.putString("scanOutput",output);
				extra.putInt("total_avs",total_avs);
				intent.putExtras(extra);
				startActivity(intent);
			}
		});
	}

	/* HTTP Class for retrieving scan details for data_id */
	private class httpUploadCaller extends AsyncTask<Void, Void, Void>
	{
		/* Define Progress Dialog Box */
		ProgressDialog pdLoading = new ProgressDialog(ReportInfoActivity.this);
		String d_id;

		public httpUploadCaller(String data_id){
			d_id = data_id;
		}

		protected void onPreExecute() {
			super.onPreExecute();

			/* Initialize Progress Dialog Box */
			pdLoading.setMessage("\tLoading... \nPlease wait ...");
			pdLoading.show();
		}

		protected Void doInBackground(Void... params) {

			/* URL with data_id to fetch scan results */
			String getURL = URL+"/"+d_id;

			try {
				/* HTTP Client */
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(getURL);
				httpGet.setHeader("apikey", API_KEY);

				/* Checking HTTP Response Code*/
				HttpResponse httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode()== 401 || httpResponse.getStatusLine().getStatusCode() == 403) {
					status_code= 401;
				}
				if (httpResponse.getStatusLine().getStatusCode()== 500) {
					status_code= 500;
				}
				else if (httpResponse.getStatusLine().getStatusCode()== 200) {
					HttpEntity httpEntity = httpResponse.getEntity();
					output = EntityUtils.toString(httpEntity);

					/* Parsing JSON Object and storing relevant information */
					data_obj = new JSONObject(output);
					file_id = data_obj.getString("file_id");
					String scan_results = data_obj.getString("scan_results");
					JSONObject scanObj = new JSONObject(scan_results);
					
					percentage = scanObj.getInt("progress_percentage");
					total_time = scanObj.getInt("total_time");
					total_avs = scanObj.getInt("total_avs");
					scan_all_result_a = scanObj.getString("scan_all_result_a");
					start_time = scanObj.getString("start_time");
				}
				return null;
			}
			catch(Exception e){
				Log.e("ERROR", e.toString());
				return null;
			}

		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			/* Dismiss Progress Dialog Box*/
			pdLoading.dismiss();

			/* If Status Code == 40x */
			if (status_code == 401) {
				Toast.makeText(getBaseContext(), "Invalid API key / Exceeded usage", Toast.LENGTH_LONG).show();
				report_info_label.setText("Invalid API key / Exceeded usage");
				report_info_label.setGravity(Gravity.CENTER_HORIZONTAL);
				moreDetailsButton.setVisibility(View.INVISIBLE);
			}
			/* If Status Code == 50x */
			if (status_code == 500) {
				Toast.makeText(getBaseContext(), "Server temporary unavailable. Try again later", Toast.LENGTH_LONG).show();
				report_info_label.setText("Server temporary unavailable. Try again later");
				report_info_label.setGravity(Gravity.CENTER_HORIZONTAL);
				moreDetailsButton.setVisibility(View.INVISIBLE);
			}
			/* If File_Id == null, then scan is in progress */
			else if (file_id.equalsIgnoreCase("null"))
			{
				Toast.makeText(getBaseContext(), "Scanning is under process. Kindly check after some time.", Toast.LENGTH_LONG).show();
				report_info_label.setText("Scanning is under process. Kindly check after some time.");
				report_info_label.setGravity(Gravity.CENTER_HORIZONTAL);
				moreDetailsButton.setVisibility(View.INVISIBLE);
			}
			
			/* If Scanning of File is complete and result is available */
			else if (status_code == 200)
			{
				/* Storing information to display in String */
				info_to_display ="<b> File ID- </b>"+file_id+" <br/><br/>"+
						"<b> Start Time- </b>"+start_time+" <br/><br/>"+
						"<b> Total Time (in ms)- </b>"+total_time+" <br/><br/>"+
						"<b> Total AVS- </b>"+total_avs+" <br/><br/>"+
						"<b> Percentage- </b>"+percentage+"% <br/><br/>"+
						"<b> Scan Result- </b>"+scan_all_result_a+"<br/>";

				/* If File is clean, then set Image Icon with 'Right' Image */
				if (scan_all_result_a.contains("Clean")){
					icon_image.setImageResource(R.drawable.tick);
					icon_image.setVisibility(View.VISIBLE);
				}
				else{
					icon_image.setImageResource(R.drawable.error);
					icon_image.setVisibility(View.VISIBLE);	
				}

				/* Set Text of TextView that is displayed to the user */
				report_info_label.setText(Html.fromHtml(info_to_display));
			}
		}
	}
}
