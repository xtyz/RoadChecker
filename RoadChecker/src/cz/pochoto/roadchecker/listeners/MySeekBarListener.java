package cz.pochoto.roadchecker.listeners;

import cz.pochoto.roadchecker.MainActivity;
import cz.pochoto.roadchecker.utils.LowPassFilter;
import android.widget.SeekBar;

public class MySeekBarListener implements SeekBar.OnSeekBarChangeListener{

	private String type;
	
	public MySeekBarListener(String type) {
		this.type = type;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		LowPassFilter.ALPHA = progress / 1000f;
		if(type == "M"){
			if(MainActivity.barG != null) MainActivity.barG.setProgress(progress);
		}
		if(type == "G"){
			if(MainActivity.barM != null) MainActivity.barM.setProgress(progress);
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

}
