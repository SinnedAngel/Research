package com.example.research;

import java.util.ArrayList;

import com.example.research.AddPhotoView.PictureItem;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity
{
	private AddPhotoView mAddPhotoView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAddPhotoView = (AddPhotoView) findViewById(R.id.addPhotoView1);
		mAddPhotoView.setPicturePath("TestPhoto");
		mAddPhotoView.setImageSize(100);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		if (mAddPhotoView != null)
		{
			String[] uriList = mAddPhotoView.getUriList();

			outState.putStringArray("IMAGES", uriList);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);

		if (mAddPhotoView != null)
		{
			String[] uriList = savedInstanceState.getStringArray("IMAGES");
			if (uriList != null)
			{
				mAddPhotoView.addImages(uriList);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (mAddPhotoView != null)
			mAddPhotoView.onActivityResult(requestCode, resultCode, data);
	}
}
