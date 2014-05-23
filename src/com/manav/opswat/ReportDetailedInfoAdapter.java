/*
 * @author manavsinghal
 *	ReportDetailedInfoAdapter.java - This is Adapter class for ReportDetailedInfo class, 
 *									 that shows the detailed info of the scanned record.
 */

package com.manav.opswat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportDetailedInfoAdapter extends BaseAdapter {
    
	private Activity activity;
	private String[] avName_Arr, defTime_Arr, scanTime_Arr, threat_Arr;
	private int[] icon;
	private static LayoutInflater inflater=null;
	
    public ReportDetailedInfoAdapter(Activity a, int[] d, String[] avName, String[] defTime, String[] scanTime, String[] threat) 
    {
        activity = a;
        icon=d;
        avName_Arr=avName;
        defTime_Arr= defTime;
        scanTime_Arr= scanTime;
        threat_Arr= threat;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return avName_Arr.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    /* View Holder Object */
    public static class ViewHolder{
    	public ImageView tickIcon;
		public TextView AVName_box, DefTime_box, ScanTime_box, ThreatInfo_box;
    }

    /* Customized View for ListView */
    public View getView(int position, View convertView, ViewGroup parent) {
    	View vi=convertView;
    	ViewHolder holder;
    	
		if(convertView==null)
		{
			vi = inflater.inflate(R.layout.report_item, null);
			holder=new ViewHolder();
			
			/* Initializing UI Widgets in custom ListView layout */
			holder.tickIcon = (ImageView)vi.findViewById(R.id.tickImage);
			holder.AVName_box = (TextView)vi.findViewById(R.id.AVName);
			holder.DefTime_box = (TextView)vi.findViewById(R.id.DefTime);
			holder.ScanTime_box = (TextView)vi.findViewById(R.id.ScanTime);
			holder.ThreatInfo_box = (TextView)vi.findViewById(R.id.ThreatInfo);
			vi.setTag(holder);
		}
		else
			/* Setting Text for UI Widgets of custom Listview layout */
			holder=(ViewHolder)vi.getTag();
			holder.tickIcon.setImageResource(icon[position]);
			holder.AVName_box.setText(avName_Arr[position]);
			holder.DefTime_box.setText(defTime_Arr[position]);
			holder.ScanTime_box.setText(scanTime_Arr[position]+" ms");
			holder.ThreatInfo_box.setText("Threat Found: "+threat_Arr[position]);
			
        
		return vi;
		
    }
}