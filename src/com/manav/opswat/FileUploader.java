/*
 * @author manavsinghal
 *	FileUploader.java - This class uploads the files or folders to the cloud using Metascan REST API web services.
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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
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

public class FileUploader extends Activity {

	private File currentDir;
	private FileArrayAdapter adapter;
	private ListView fileList;

	AlertDialog.Builder filebuilder, folderBuilder;
	AlertDialog fileAlert, folderAlert; 
	String file_Location, fileName, data_id, imageDataString, response_data, last_data_id, folder_Location;
	InputStream inputStream;
	JSONObject jsonObject;
	ImageView settings_button;

	private static InputStream in = null;
	DBAdapter dataAdapter;
	ArrayList<String> files = new ArrayList<String>();
	boolean folderFlag = false;
	TextView currentPathLabel;
	
	/* Declaring Shared Preferences */
	SharedPreferences pref; // 0 - for private mode
	Editor editor;
	String API_KEY;

	/* Defines REST API URL */
	private String URL = "https://api.metascan-online.com/v1/file";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_upload);

		/* Initialize UI Widgets */
		currentPathLabel = (TextView) findViewById(R.id.heading_desc);
		currentPathLabel.setText("/sdcard/");
		fileList= (ListView) findViewById(R.id.file_list);
		settings_button= (ImageView) findViewById(R.id.setings_button);
		dataAdapter = new DBAdapter(this);

		/* Initializing Shared Preferences */
		pref = getApplicationContext().getSharedPreferences("api_pref", MODE_PRIVATE);
		editor = pref.edit();
		API_KEY = pref.getString("Api_Key", null);
		
		/* OnClick Handler for Settings Button */
		settings_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),Settings.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
		
		/* Initialize Alert Dialog box (Yes/No) for asking user to scan File or not */
		filebuilder = new AlertDialog.Builder(this);
		filebuilder.setTitle("Confirm").setIcon(R.drawable.warning);
		filebuilder.setMessage("Are you sure to scan this file?");
		filebuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				/* Checking Internet connection, If available then only do HTTP call */
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
				if(cd.isConnectingToInternet() && API_KEY != null){
					/* Making Http Call to upload file */
					httpUploadCaller httpUpload = new httpUploadCaller(file_Location, folderFlag);
					httpUpload.execute();
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
		filebuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		fileAlert = filebuilder.create();

		/* Initialize Alert Dialog box (Yes/No) for asking user to scan Folder or not */
		folderBuilder = new AlertDialog.Builder(this);
		folderBuilder.setTitle("Confirm").setIcon(R.drawable.warning);
		folderBuilder.setMessage("Are you sure to scan this folder?");
		folderBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) 
			{
				/* Checking Internet connection, If available then only do HTTP call */
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
				if(cd.isConnectingToInternet() && API_KEY != null ){
					/* Making HTTP call to upload folder */
					folderFlag = true;
					httpUploadCaller httpUpload = new httpUploadCaller(folder_Location,folderFlag);
					httpUpload.execute();
				}
				/* If Internet not connected */
				else if (cd.isConnectingToInternet() != true){
					Toast.makeText(getBaseContext(), "Please check the internet connection.", Toast.LENGTH_LONG).show();	
				}
				/* If API Key is NULL or not set */
				else if (API_KEY == null){
					Toast.makeText(getBaseContext(), "API Key Not Set", Toast.LENGTH_LONG).show();
				}
			}
		});
		folderBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		folderAlert = folderBuilder.create();

		/* First time list filling of FileExplorer with '/sdcard/' path */
		currentDir = new File("/sdcard/");
		fill(currentDir); 

		/* OnClick Handler for File Upload*/
		fileList.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) 
			{
				Item o = adapter.getItem(position);
				if(o.getImage().equalsIgnoreCase("directory_icon")||o.getImage().equalsIgnoreCase("directory_up")){
					currentDir = new File(o.getPath());
					currentPathLabel.setText(o.getPath()+"/");
					fill(currentDir);
				}
				else{
					onFileClick(o);
				}
			}
		});

		/* Click Handler for Folder Upload */
		fileList.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
				Item o = adapter.getItem(position);
				if(o.getImage().equalsIgnoreCase("directory_icon")){
					folder_Location = o.getPath();
					folderAlert.show();
				}
				return true;
			}
		});

	}

	/* Method to list all files inside any directory */
	private void listf(String directoryName)
	{
		File directory = new File (directoryName);

		File[] flist = directory.listFiles();
		for (File file : flist) {
			if (file.isFile())
			{
				files.add(file.getAbsolutePath());
			} else if (file.isDirectory()) {
				listf(file.getAbsolutePath());
			}
		}
	}

	/* Method to fill File Explorer list with folder/file paths */
	private void fill(File f)
	{
		File[]dirs = f.listFiles(); 
		List<Item>dir = new ArrayList<Item>();
		List<Item>fls = new ArrayList<Item>();
		try{
			for(File ff: dirs){ 
				Date lastModDate = new Date(ff.lastModified()); 
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if(ff.isDirectory()){

					File[] fbuf = ff.listFiles(); 
					int buf = 0;
					if(fbuf != null){ 
						buf = fbuf.length;
					} 
					else buf = 0; 
					String num_item = String.valueOf(buf);
					if(buf == 0) num_item = num_item + " item";
					else num_item = num_item + " items";

					dir.add(new Item(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"directory_icon")); 
				}
				else{
					fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"file_icon"));
				}
			}
		}catch(Exception e){    
			Log.e("ERROR",e.toString());
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if(!f.getName().equalsIgnoreCase("sdcard"))
			dir.add(0,new Item("..","Parent Directory","",f.getParent(),"directory_up"));

		// File list Adapter for File Explorer
		adapter = new FileArrayAdapter(FileUploader.this,R.layout.file_view,dir);
		fileList.setAdapter(adapter); 
	}

	// Method called if any file is clicked
	private void onFileClick(Item o){
		file_Location = o.getPath();
		String file = o.getName();
		fileName = file.substring(0, file.indexOf("."));

		/* Displaying Alert Dialog box confirming the user to upload or not */
		fileAlert.show();
	}

	/* Method to save Data_Id and file path for uploaded file in Database */
	private void saveDataID (String DATA_ID, String FILE_PATH)
	{
		try{
			/* Database Adapter Connection open */
			dataAdapter.open();
			Cursor cur=dataAdapter.getAllTitles();

			/* Check for 100 last records. In case of more than 100 records, it will delete last record (as per timestamp). */
			if (cur.getCount() > 99){
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

	/* Method for making HTTP Request to Metascan REST API to upload the file content */
	private class httpUploadCaller extends AsyncTask<Void, Void, Void>
	{
		/* Declare Progress Dialog Box*/
		ProgressDialog pDialog = new ProgressDialog(FileUploader.this);
		String PATH, fileData;
		boolean folderFLAG;

		public httpUploadCaller(String Location, boolean folderFlag)
		{
			/* PATH represents file path or directory path 
			 * folderFLAG represents whether file upload or folder upload */
			PATH = Location;
			folderFLAG = folderFlag;
		}

		protected void onPreExecute() {
			super.onPreExecute();

			/* Progress Dialog Box Initialization */
			pDialog.setMessage("\tUploading...");
			pDialog.setIndeterminate(false);
			pDialog.setMax(100);
			pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		protected Void doInBackground(Void... params) {

			/* Check for File upload or Folder upload
			 * If folderFLAG == false, then file upload
			 * else folder upload */

			if (folderFLAG == false)
			{
				/* Check for Images as files */
				if (PATH.toLowerCase().endsWith(".jpg") || PATH.toLowerCase().endsWith(".jpeg") || PATH.toLowerCase().endsWith(".png") ||
						PATH.toLowerCase().endsWith(".bmp") || PATH.toLowerCase().endsWith(".gif") || PATH.toLowerCase().endsWith(".webp"))
				{
					/* Getting Image Content and store as String */
					fileData = FileHandling.getImageContent(PATH);
					pDialog.setProgress(30);
				}
				else{
					/* Getting File Content and store as String */
					fileData = FileHandling.getFileContent(PATH);
					pDialog.setProgress(30);
				}

				/* HTTP Client */ 
				HttpParams myParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(myParams, 10000);
				HttpConnectionParams.setSoTimeout(myParams, 10000);
				HttpClient httpclient = new DefaultHttpClient();

				try {
					/* HTTP Post and set header */
					HttpPost httppost = new HttpPost(URL.toString());
					httppost.setHeader("apikey", API_KEY);
					httppost.setHeader("filename", fileName);

					/* Setting file content as post body */
					StringEntity se = new StringEntity(fileData); 
					httppost.setEntity(se); 
					HttpResponse response = httpclient.execute(httppost);
					Log.e("STATUS",""+response.getStatusLine().getStatusCode());

					/* Check Status Code for Response */
					if (response.getStatusLine().getStatusCode() == 401 || response.getStatusLine().getStatusCode() == 403){
						data_id="401";
					}
					else if (response.getStatusLine().getStatusCode() == 500){
						data_id="500";
					}
					else{
						in = response.getEntity().getContent();
						if(in != null)
							response_data = FileHandling.convertInputStreamToString(in);
						else
							response_data = "Did not work!";

						jsonObject = new JSONObject(response_data);
						data_id = jsonObject.getString("data_id");

						pDialog.setProgress(80);
						/* Method call for saving data_id into database */
						saveDataID(data_id, PATH);
						pDialog.setProgress(100);
					}

				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
					return null;
				} 
			}
			else
			{
				/* Method call for listing all files within directory */
				listf(PATH);

				int filesCount = files.size();
				int count = 0;

				/* Processing each file within the folder */
				for (String eachfileLocation : files)
				{
					if (eachfileLocation.toLowerCase().endsWith(".jpg") || eachfileLocation.toLowerCase().endsWith(".jpeg") || eachfileLocation.toLowerCase().endsWith(".png") ||
							eachfileLocation.toLowerCase().endsWith(".bmp") || eachfileLocation.toLowerCase().endsWith(".gif") || eachfileLocation.toLowerCase().endsWith(".webp")){
						/* Getting Image Content and store as String */
						fileData = FileHandling.getImageContent(eachfileLocation);
					}
					else{
						/* Getting File Content and store as String */
						fileData = FileHandling.getFileContent(eachfileLocation);	
					}

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

							/* Saving data_id into database for each successfully uploaded file */
							saveDataID(data_id, eachfileLocation);

							count++;
							if (count <= filesCount)
							{
								/* Set Progress for Progress Bar */
								pDialog.setProgress(Integer.parseInt(""+(int)((count*100)/filesCount)));
							}
						}

					} catch (Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
						return null;
					}
				}
			}

			return null;
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			/* Dismiss Progress Dialog Box*/
			pDialog.dismiss();

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
			/* Confirmation Message for successful file upload */
			else if (folderFLAG == false && data_id != null && data_id != "NIL") {
				Toast.makeText(getBaseContext(), "Your file has been successfully uploaded for virus scan."
						+ "Kindly check scan reports after some time.", Toast.LENGTH_SHORT).show();	
			}
			/* Confirmation Message for successful folder upload */
			else if (folderFLAG == true && data_id != null && data_id != "NIL"){
				Toast.makeText(getBaseContext(), "Your folder has been successfully uploaded for virus scan."
						+ "Kindly check scan reports for each file after some time.", Toast.LENGTH_SHORT).show();	
			}
		}

	}
}
