/*
 * @author manavsinghal
 *	ReportDetailedInfo.java - This class represents the detailed information for the file scanned with upto 40 Anti Virus Engines.
 */

package com.manav.opswat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.manav.opswat.RecordsActivity.RecordAdapter;
import com.manav.opswat.RecordsActivity.ViewHolder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReportDetailedInfo extends Activity {

	String output= null;
	int total_avs;
	JSONObject data_obj;
	String[] AVName_array, defTime_array, threatInfo_array, scanTime_array;
	int[] icon_images;

	ListView reportList;
	ImageView icon_image,titleIcon;

	/* Define Anti-Virus Engines */
	String[] AV_list = {"AegisLab","Agnitum","Ahnlab","Antiy","AVG","Avira","BitDefender","ByteHero","ClamWin","Commtouch",
			"Emsisoft","ESET","F-prot","F-secure","Filseclab","Fortinet","Hauri","Ikarus","Jiangmin","K7",
			"Kaspersky","Kingsoft","Lavasoft","McAfee","Microsoft","NANO","Norman","nProtect","QuickHeal","Sophos",
			"STOPzilla","SUPERAntiSpyware","Symantec","ThreatTrack","TotalDefense","TrendMicro","TrendMicroHouseCall",
			"VirIT","VirusBlokAda","Zillya!"};

	ReportDetailedInfoAdapter reportListAdapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_layout);

		Bundle extra=this.getIntent().getExtras();
		output = extra.getString("scanOutput");
		total_avs = extra.getInt("total_avs");

		/* Define Array for Anti-Virus Name, Definition Time, Scanned Time and Threat Info Array */
		AVName_array = new String[total_avs];
		defTime_array = new String[total_avs];
		scanTime_array = new String[total_avs];
		threatInfo_array = new String[total_avs];

		/* Parsing of the JSONObject and retrieving useful information */
		try{
			data_obj = new JSONObject(output);
			JSONObject scanObj = new JSONObject(data_obj.getString("scan_results"));
			JSONObject scanDetailObject = new JSONObject(scanObj.getString("scan_details"));

			int count=0;
			/* Processing JSONObject for each Anti-Virus Engine */
			for (int i=0; i< AV_list.length; i++)
			{
				JSONObject avJSONObj = null;
				if (scanDetailObject.has(AV_list[i])){		
					avJSONObj = new JSONObject(scanDetailObject.getString(AV_list[i]));
					AVName_array[count] = AV_list[i];
					defTime_array[count] = avJSONObj.getString("def_time");
					scanTime_array[count] = avJSONObj.getString("scan_time");

					if (avJSONObj.getString("threat_found").equalsIgnoreCase("null") || avJSONObj.getString("threat_found").trim().length() == 0){
						threatInfo_array[count] = "Nil";
					}
					else{
						threatInfo_array[count] = avJSONObj.getString("threat_found");
					}
					count++;
				}
			}

			/* Define List for Anti-Virus Engines without null values */
			List<String> list = new ArrayList<String>();
			for(String s : AVName_array) {
				if(s != null && s.length() > 0) {
					list.add(s);
				}
			}
			AVName_array = list.toArray(new String[list.size()]);

		}
		catch(Exception e){
			Log.e("Error",e.toString());
		}

		/* Setting Image Icon for 'Right' or 'Wrong' icon as per Threat Found 
		 * If Thread Not found, set 'Right' icon Image
		 * If Threat Found, set 'Wrong' icon Image */
		
		icon_images=new int[total_avs];
		for (int i=0;i<AVName_array.length;i++)
		{
			if (threatInfo_array[i].equalsIgnoreCase("Nil")){
				icon_images[i]=R.drawable.tick;
			}
			else{
				icon_images[i]=R.drawable.error;
			}
		}

		/* ListView UI Initialization and Set Adapter for ListView */
		reportList=(ListView) findViewById(R.id.report_list);
		reportListAdapter=new ReportDetailedInfoAdapter(ReportDetailedInfo.this,icon_images,AVName_array,defTime_array,scanTime_array,threatInfo_array);
		reportList.setAdapter(reportListAdapter);

		/* OnClick Handler for Menu button at top-right corner of screen */
		titleIcon=(ImageView) findViewById(R.id.title_icon);
		titleIcon.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}

}
