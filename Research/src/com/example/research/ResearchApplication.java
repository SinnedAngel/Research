package com.example.research;

import android.app.Application;
import android.os.Environment;

public class ResearchApplication extends Application
{
	public ResearchApplication()
	{
		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(Environment.getExternalStorageDirectory()
				.getPath()));
	}
}
