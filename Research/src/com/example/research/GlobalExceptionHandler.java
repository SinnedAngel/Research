package com.example.research;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;

public class GlobalExceptionHandler implements UncaughtExceptionHandler
{
	// TODO disable this on release

	private String mPath;

	public GlobalExceptionHandler(String path)
	{
		mPath = path + "/research_crash_report/";
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex)
	{
		if (ex != null)
		{
			File file = new File(mPath);
			if (!file.exists())
				file.mkdirs();

			FileOutputStream fos = null;
			try
			{
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");

				fos = new FileOutputStream(mPath + format.format(date) + ".txt", true);
				fos.write(ex.getMessage().getBytes());
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
			}
			catch (Exception e)
			{
				// TODO
			}
			finally
			{
				try
				{
					if (fos != null)
						fos.close();
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
			}
		}
	}

}
