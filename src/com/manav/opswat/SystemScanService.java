/*
 * @author manavsinghal
 *	SystemScanService.java - This service runs in background and reads every file, upload it online 
 *							 and then the user can view the results for scanned files.
 */

package com.manav.opswat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList; 
import java.util.Collections;
import java.util.List;
import java.text.DateFormat; 

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle; 
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent; 
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView; 
import android.widget.TextView;
import android.widget.Toast;

public class SystemScanService extends Service {

	/* Declaring Variables */
	String file_Location, fileName, data_id, imageDataString, response_data, last_data_id, folder_Location;
	InputStream inputStream;
	JSONObject jsonObject;

	private static InputStream in = null;
	DBAdapter dataAdapter;
	ArrayList<String> files = new ArrayList<String>();

	/* Declaring Shared Preferences */
	SharedPreferences pref; // 0 - for private mode
	Editor editor;
	String API_KEY;

	/* Defines REST API URL */
	private String URL = "https://api.metascan-online.com/v1/file";
	httpUploadCaller httpUpload;

	/* onCreate() method for Service Creation */
	public void onCreate() {

		dataAdapter = new DBAdapter(this);
		/* Initializing Shared Preferences */
		pref = getApplicationContext().getSharedPreferences("api_pref", MODE_PRIVATE);
		editor = pref.edit();
		API_KEY = pref.getString("Api_Key", null);
	}

	/* onDestroy() method for Service Stopping */
	public void onDestroy() {
		Toast.makeText(this, "System Scanning Service is Stopped", Toast.LENGTH_LONG).show();
		httpUpload.cancel(true);
	}

	/* onStart() method for Service Start */
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "System Scanning Service Started", Toast.LENGTH_LONG).show();
		
		/* Http AsyncTask method call */
		httpUpload = new httpUploadCaller("/sdcard/");
		httpUpload.execute();
	}

	/* Method to list all files inside any directory */
	private void listf(String directoryName)
	{
		File directory = new File (directoryName);

		File[] flist = directory.listFiles();
		for (File file : flist) {
			if (file.isFile()){
				files.add(file.getAbsolutePath());
			} else if (file.isDirectory()) {
				listf(file.getAbsolutePath());
			}
		}
	}

	/* Method to save Data_Id and file path for uploaded file in Database */
	private void saveDataID (String DATA_ID, String FILE_PATH)
	{
		try{
			/* Database Adapter Connection open */
			dataAdapter.open();
			Cursor cur=dataAdapter.getAllTitles();

			/* Check for 200 last records. In case of more than 200 records, it will delete last record (as per timestamp). */
			if (cur.getCount() > 4999){
				cur.moveToLast();
				last_data_id=cur.getString(1);
				dataAdapter.deleteTitle(last_data_id);
			}

			/* Database Insert Query */
			Long i = dataAdapter.insertTitle(DATA_ID, FILE_PATH);
			dataAdapter.close();
		}
		catch (Exception e){
			Toast.makeText(getBaseContext(), "Error\n"+ e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	/* Method to get File Size (in MB) */
	private double getFileSize(String filePath){
		File file = new File(filePath);
		double bytes = file.length();
		double kilobytes = (bytes / 1024);
		double megabytes = (kilobytes / 1024);
		return megabytes;
	};

	/* Method for making HTTP Request to Metascan REST API to upload the file content */
	private class httpUploadCaller extends AsyncTask<Void, Void, Void>
	{
		/* Declare Progress Dialog Box*/
		String PATH, fileData;

		public httpUploadCaller(String Location){
			/* PATH represents file path or directory path */
			PATH = Location;
		}

		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected Void doInBackground(Void... params) {

			/* Method call for listing all files within directory */
			listf(PATH);

			int filesCount = files.size();
			Log.e("COUNT",filesCount+"");
			int count = 0;

			/* Processing each file within the folder */
			for (String eachfileLocation : files)
			{
				/* If Service is stopped, then stop loop execution */
				if (isCancelled())
					break;

				/* Getting File Size for each file */
				double fileSize = getFileSize(eachfileLocation);

				/* If File Size is less than 10 MB, then only continue */
				if (fileSize > 10){
					continue;
				}

				fileData = null;
				/* If File is Image, retrieve its content */
				if (eachfileLocation.toLowerCase().endsWith(".jpg") || eachfileLocation.toLowerCase().endsWith(".jpeg") || eachfileLocation.toLowerCase().endsWith(".png") ||
						eachfileLocation.toLowerCase().endsWith(".bmp") || eachfileLocation.toLowerCase().endsWith(".gif") || eachfileLocation.toLowerCase().endsWith(".webp")){
					/* Getting Image Content and store as String */
					fileData = FileHandling.getImageContent(eachfileLocation);
				}
				else{
					/* Getting File Content and store as String */
					fileData = FileHandling.getFileContent(eachfileLocation);	
				}

				/* Retrieving File Name */
				fileName = new File(eachfileLocation).getName();

				/* HTTP Client */
				HttpParams myParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(myParams, 10000);
				HttpConnectionParams.setSoTimeout(myParams, 10000);
				HttpClient httpclient = new DefaultHttpClient();

				try {
					/* HTTP Post and set Header */
					HttpPost httppost = new HttpPost(URL.toString());
					httppost.setHeader("apikey", API_KEY);
					httppost.setHeader("filename", fileName);
					StringEntity se = new StringEntity(fileData); 
					httppost.setEntity(se); 
					HttpResponse response = httpclient.execute(httppost);
					in = response.getEntity().getContent();

					/* Check Status Code for Response */
					if (response.getStatusLine().getStatusCode() == 401 || response.getStatusLine().getStatusCode() == 403){
						data_id="401";
					}
					else if (response.getStatusLine().getStatusCode() == 500){
						data_id="500";
					}
					else if (response.getStatusLine().getStatusCode() == 200){
						if(in != null)
							response_data = FileHandling.convertInputStreamToString(in);
						else
							response_data = "Did not work!";

						jsonObject = new JSONObject(response_data);
						data_id = jsonObject.getString("data_id");
						Log.e("DATA_ID",data_id);

						/* Saving data_id into database for each successfully uploaded file */
						saveDataID(data_id, eachfileLocation);
						count++;
					}
				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
					return null;
				}
			}
			
			return null;
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			/* In case due to some reason, data_id == null */
			if (data_id == null){
				Toast.makeText(getBaseContext(), "File could not be uploaded due to some error. Kindly try once again.", Toast.LENGTH_LONG).show();
			}
			/* In case of Response Status Code 40x */
			else if (data_id.equalsIgnoreCase("401")){
				Toast.makeText(getBaseContext(), "Invalid API key / Exceeded usage", Toast.LENGTH_LONG).show();
			}
			/* In case of Response Status Code 50x */
			else if (data_id.equalsIgnoreCase("500")){
				Toast.makeText(getBaseContext(), "Server temporary unavailable. Try again later", Toast.LENGTH_LONG).show();
			}
			/* Confirmation Message for successful folder upload */
			else if (data_id != null && data_id != "NIL"){
				 Toast.makeText(getBaseContext(), "Your system has been scanned successfully for virus scan."
					+ "Kindly check scan reports for each file after some time.", Toast.LENGTH_SHORT).show();
				Log.i("DATA_ID", data_id);
			}
		}

	}

	public IBinder onBind(Intent intent) {
		return null;
	}
}
