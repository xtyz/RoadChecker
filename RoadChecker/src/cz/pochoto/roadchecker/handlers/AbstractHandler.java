package cz.pochoto.roadchecker.handlers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractHandler {
	
	/**
	 * Returns root view of fragment
	 * @param inflater
	 * @param container
	 * @return
	 */
	public abstract View getRootView(LayoutInflater inflater, ViewGroup container);
	
	/**
	 * Initializing base operations and views for rootview
	 */
	public abstract void init();
}
