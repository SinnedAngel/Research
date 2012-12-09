package com.example.research;

import android.app.Activity;
import android.util.DisplayMetrics;

public class MathTools
{
	public static float dpToPixel(Activity context, int dp)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float logicalDensity = metrics.density;

		return dp * (logicalDensity / 160);
	}
}