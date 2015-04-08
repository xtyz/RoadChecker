package cz.pochoto.roadchecker.utils;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class RecordUtils {

	private Context context;
	
	String fileName;
	StringBuilder stringBuilder;

	public RecordUtils(Context context) {
		this.context = context;
	}
	
	public void startRecord(String fileName){
		this.fileName = "RoadChecker" + fileName + ".txt";
		Toast.makeText(context, "Start recording "+this.fileName, Toast.LENGTH_LONG).show();
		stringBuilder = new StringBuilder();
		System.out.println("Recording started");
	}
	
	public void addValue(String value){
		stringBuilder.append(value);
		stringBuilder.append("\n");
	}
	
	public void stopRecord(){
		saveFile();
		System.out.println("Recording stoped, file saved as: "+this.fileName);
		Toast.makeText(context, "Recording stoped, file saved as: "+this.fileName, Toast.LENGTH_LONG).show();
	}
	
	@SuppressLint("WorldReadableFiles")
	private void saveFile(){
		try {
			String root=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();				
			File dir = new File(root, "/RoadChecker");			
			dir.mkdirs();
			File file = new File(dir, fileName);
			if (file.exists())file.delete();
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(stringBuilder.toString().getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
