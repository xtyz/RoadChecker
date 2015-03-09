package cz.pochoto.roadchecker.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractHandler {
	
	public abstract View getRootView(LayoutInflater inflater, ViewGroup container);
	
	public abstract void init();
}
