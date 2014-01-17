/*
 * @author manavsinghal
 *	RecordsActivity.java - This class represents the list of records for the files successfully uploaded online to view scan results.
 */

package com.manav.opswat;

import java.text.DateFormat;
import java.util.Date;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecordsActivity extends Activity {

	private ListView recordList;
	DBAdapter dataAdapter;
	String[] data_ids, fileLocations, scanned_at, fileNames;
	String data_id_to_delete;
	TextView title, titleDesc;
	ImageView settings_button;

	/* Declare Alert Dialog Box - For deleting record */
	AlertDialog.Builder builder;
	AlertDialog alert;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_upload);

		/* Initialize UI Widgets */
		recordList= (ListView) findViewById(R.id.file_list);
		title = (TextView) findViewById(R.id.heading_label);
		title.setText("Scanned Reports");
		titleDesc = (TextView) findViewById(R.id.heading_desc);
		titleDesc.setVisibility(View.GONE);
		settings_button = (ImageView) findViewById(R.id.setings_button);

		/* Initialize Alert Dialog Box for Deleting any record */
		builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm");
		builder.setIcon(R.drawable.error);
		builder.setMessage("Are you sure you want to delete this record?");
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				try{
					/* Open Database connection and deleting the record */
					dataAdapter.open();
					dataAdapter.deleteTitle(data_id_to_delete);
					dataAdapter.close();

					/* After deleting record, reload the data from database in the list */
					dataLoadMethod();
					recordList.setAdapter(new RecordAdapter(RecordsActivity.this));
				}
				catch(Exception e){
					Log.e("ERROR",e.toString());
				}
			}
		});
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		alert = builder.create();
		dataAdapter =new DBAdapter(this);

		/* Method call for filling the list of records from the database. */
		dataLoadMethod();
		recordList.setAdapter(new RecordAdapter(this));

		/* OnClick Handler for details of any scanned record */
		recordList.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
			{
				/* Launch Activity with scan report details of clicked record */
				Bundle extra=new Bundle();
				Intent intent=new Intent(getBaseContext(),ReportInfoActivity.class);
				extra.putString("data_id", data_ids[position]);
				intent.putExtras(extra);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		/* OnLongClick Handler for deleting any record from the database */
		recordList.setOnItemLongClickListener(new OnItemLongClickListener() 
		{
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
			{	
				/* Show Alert Dialog Box for confirming record to delete */
				data_id_to_delete=data_ids[position];
				alert.show();
				return true;
			}
		});

		settings_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),Settings.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});

	}

	/* Method definition to load saved records into the list from the database */
	private void dataLoadMethod() {	
		try {
			dataAdapter.open();
			Cursor cur=dataAdapter.getAllTitles();

			/* Defining variables for storing column values */
			data_ids= new String[cur.getCount()];
			fileLocations= new String[cur.getCount()];
			fileNames= new String[cur.getCount()];
			scanned_at= new String[cur.getCount()];
			cur.moveToFirst();

			int i=0;
			while (cur.isAfterLast() == false) {
				data_ids[i]=cur.getString(1);

				String file=cur.getString(2);
				fileLocations[i] = file.substring(0, file.lastIndexOf("/")+1);
				fileNames[i]= file.substring(file.lastIndexOf("/")+1, file.length());

				scanned_at[i]=DateFormat.getDateTimeInstance().format(new Date(cur.getLong(3)));
				i++;
				cur.moveToNext();
			}
			/* Closing Database Connection */
			dataAdapter.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* View Holder definition for Base Adapter */
	public class ViewHolder 
	{
		ImageView icon;
		TextView firstLine, secondLine, thirdLine;
	}

	/* Definition of Adapter for custom ListView Layout */
	public class RecordAdapter extends BaseAdapter
	{
		int previousselectedposition=-1;
		Context mContext;
		public static final int ACTIVITY_CREATE = 10;
		public RecordAdapter(Context c){	
			mContext = c;
		}

		public int getCount(){
			return fileNames.length;
		}

		public View getView(int position, View convertView, ViewGroup parent){
			ViewHolder holder;
			/* Custom Layout of ListView */
			if(convertView==null)
			{
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.record_item, parent, false);

				/* ViewHolder Object for containing UI Widgets in Listview */
				holder=new ViewHolder();
				holder.icon = (ImageView)convertView.findViewById(R.id.icon);
				holder.firstLine = (TextView)convertView.findViewById(R.id.firstLine);
				holder.secondLine = (TextView)convertView.findViewById(R.id.secondLine);
				holder.thirdLine = (TextView)convertView.findViewById(R.id.thirdLine);

				holder.firstLine.setText(fileNames[position]);
				holder.secondLine.setText(fileLocations[position]);
				holder.thirdLine.setText(scanned_at[position]);
			}
			else{
				holder = (ViewHolder) convertView.getTag();
			}
			return convertView;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
	}
}
