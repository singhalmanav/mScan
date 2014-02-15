/*
 * @author manavsinghal
 *	FileHandling.java - This class is helper class for handling the files such as-
 *						- To get content of Image file
 *						- To get content of any file
 *						- To convert InputStream to String
 */

package com.manav.opswat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Base64;
import android.util.Log;

public class FileHandling {

	public static String imageDataString;
	public static InputStream inputStream;

	public static void main(String[] args) {}

	/* Method to get Content of Images (.jpg,.bmp,.png etc) 
	 * returns image content in String */
	public static String getImageContent(String targetImagePath)
	{
		File file = new File(targetImagePath);
		try {
			InputStream inputImageStream = new FileInputStream(file);
			byte[] bytes;
			byte[] buffer = new byte[8192];
			int bytesRead;
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			while ((bytesRead = inputImageStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			bytes = output.toByteArray();
			
			// Base64 encoding conversion to String
			imageDataString = Base64.encodeToString(bytes, Base64.DEFAULT);
			inputImageStream.close();
			return imageDataString;
		} catch (Exception e) {
			Log.e("Error",e.toString());
			return null;
		}
	}

	/* Method to get Content of Files (.pdf,.doc,.ppt etc) 
	 * returns file content in String */
	public static String getFileContent(String targetFilePath)
	{
		StringBuilder sb = new StringBuilder();

		try {
			// open the file for reading
			inputStream = new FileInputStream(targetFilePath);

			if (inputStream != null) {
				InputStreamReader inputreader = new InputStreamReader(inputStream,"UTF-8");
				BufferedReader buffreader = new BufferedReader(inputreader);

				String line = null;
				while ((line = buffreader.readLine()) != null) {
					sb.append(line).append("\n");
				}
				inputStream.close();
			}
			return sb.toString();
			
		} catch (Exception ex) {
			Log.e("Error", ex.toString());
			return null;
		} 
	}
	
	/* Method to convert InputStream to String */
	public static String convertInputStreamToString(InputStream inputStream) throws IOException{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	} 

}
