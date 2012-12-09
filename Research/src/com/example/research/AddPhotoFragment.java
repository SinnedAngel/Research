package com.example.research;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.example.research.AddPhotoView.PictureItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class AddPhotoFragment extends Fragment
{
	private LinearLayout mMainLayout;
	private ImageButton mButtonAddPhoto;
	private LinearLayout mCurrentContainer;

	private LayoutParams mContainerParams;
	private LayoutParams mItemParams;

	private int mItemSize = 80;
	private int mPadding = 5;
	private int mHalfPadding = 2;

	private int mMaxColumns = 3;

	private boolean mIsUseSize = false;

	private static final int ACTION_CAMERA = 1;
	private static final int ACTION_GALLERY = 2;

	private ArrayList<String> mUriList = new ArrayList<String>();
	private ArrayList<PictureItem> mImageList = new ArrayList<PictureItem>();

	private String mPicturePath;

	private Uri mImageCaptureUri;

	public PictureItem[] getImageList()
	{
		return mImageList.toArray(new PictureItem[mImageList.size()]);
	}

	public void setPicturePath(String picturePath)
	{
		mPicturePath = Environment.getExternalStorageDirectory() + "/" + picturePath + "/";
		File file = new File(mPicturePath);
		if (!file.exists())
			file.mkdirs();
	}

	public void setMaxColumn(int maxColumn)
	{
		this.mMaxColumns = maxColumn;
		mIsUseSize = false;
	}

	public void setImageSize(int size)
	{
		mItemSize = size;
		mIsUseSize = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.add_photo_fragment, container, false);

		mMainLayout = (LinearLayout) v.findViewById(R.id.layoutMain);

		mContainerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		Activity activity = getActivity();

		mItemParams = new LayoutParams(mItemSize, mItemSize);
		// mItemParams.rightMargin = mPadding;
		mItemParams.topMargin = mPadding;
		mItemParams.leftMargin = mHalfPadding;
		mItemParams.rightMargin = mHalfPadding;

		mCurrentContainer = new LinearLayout(activity);
		mCurrentContainer.setLayoutParams(mContainerParams);
		mCurrentContainer.setOrientation(LinearLayout.HORIZONTAL);

		// int padding = mPadding / 2;
		//
		// mCurrentContainer.setPadding(padding, padding, padding, padding);
		mMainLayout.addView(mCurrentContainer);

		mButtonAddPhoto = new ImageButton(activity);
		mButtonAddPhoto.setBackgroundResource(R.drawable.upload_foto_bg);
		mButtonAddPhoto.setImageResource(R.drawable.thumbnail_upload);
		mButtonAddPhoto.setLayoutParams(mItemParams);
		mCurrentContainer.addView(mButtonAddPhoto);

		mButtonAddPhoto.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showPictureSelection();
			}
		});

		mMainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				View v = getView();

				if (v != null)
				{
					int width = v.getWidth();
					if (mIsUseSize)
						mMaxColumns = (width / mItemSize) - 1;
					else
						mItemSize = width / (mMaxColumns + 1);
					mPadding = mItemSize / mMaxColumns;
					mHalfPadding = mPadding / 2;
					mItemParams.width = mItemSize;
					mItemParams.height = mItemSize;
					// mItemParams.rightMargin = mPadding;
					mItemParams.topMargin = mPadding;
					mItemParams.leftMargin = mHalfPadding;
					mItemParams.rightMargin = mHalfPadding;
					// mButtonAddPhoto.setLayoutParams(mItemParams);
					// int padding = mPadding / 2;
					//
					// mCurrentContainer.setPadding(padding, padding, padding,
					// padding);
					mContainerParams.leftMargin = mHalfPadding;
					mContainerParams.bottomMargin = mPadding;
					if (mUriList.size() > 0)
					{
						for (String string : mUriList)
						{
							Uri uri = Uri.parse(string);
							addImage(uri);
						}

						mUriList.clear();
					}
				}
			}
		});

		return v;
	}

	@Override
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState)
	{
		super.onInflate(activity, attrs, savedInstanceState);

		final TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.AddPhotoView);

		mIsUseSize = a.getBoolean(R.styleable.AddPhotoView_isUseSize, false);
		mItemSize = (int) a.getDimension(R.styleable.AddPhotoView_itemSize, 80);
		mMaxColumns = a.getInteger(R.styleable.AddPhotoView_maxColumns, 3);
		mPicturePath = a.getString(R.styleable.AddPhotoView_picturePath);

		a.recycle();
	}

	private void addImage(Uri uri)
	{
		if (uri == null)
			return;

		Context context = getActivity();

		InputStream inputStream = null;
		try
		{
			ContentResolver cr = context.getContentResolver();
			// Bitmap bitmap = Media.getBitmap(cr, uri);
			inputStream = cr.openInputStream(uri);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

			PictureItem item = new PictureItem();
			item.image = bitmap;
			item.uri = uri;

			mImageList.add(item);

			ImageView imageView = new ImageView(context);
			imageView.setImageBitmap(bitmap);
			imageView.setBackgroundResource(R.drawable.upload_foto_bg);
			imageView.setLayoutParams(mItemParams);

			int count = mCurrentContainer.getChildCount();
			if (count > 0)
				mCurrentContainer.addView(imageView, count - 1);

			if (count + 1 > mMaxColumns)
			{
				mCurrentContainer.removeView(mButtonAddPhoto);

				mCurrentContainer = new LinearLayout(context);
				mCurrentContainer.setLayoutParams(mContainerParams);
				mCurrentContainer.setOrientation(LinearLayout.HORIZONTAL);
				// mCurrentContainer.setPadding(mPadding, mPadding, mPadding,
				// mPadding);
				mMainLayout.addView(mCurrentContainer);

				mCurrentContainer.addView(mButtonAddPhoto);
			}
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
		}
		finally
		{
			// try
			// {
			// if (inputStream != null)
			// inputStream.close();
			// }
			// catch (IOException e)
			// {
			// }
		}
	}

	private void showPictureSelection()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
		adb.setTitle("Pick a Source");
		adb.setItems(new String[] { "Camera", "Gallery" }, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
					case 0:

						if (mPicturePath == null)
						{
							mPicturePath = Environment.getExternalStorageDirectory() + "/" + mPicturePath + "/";
							File file = new File(mPicturePath);
							if (!file.exists())
								file.mkdirs();
						}

						Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

						mImageCaptureUri = Uri.fromFile(new File(mPicturePath, "tmp_avatar_"
								+ String.valueOf(System.currentTimeMillis()) + ".jpg"));

						camIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

						try
						{
							camIntent.putExtra("return-data", true);
						}
						catch (ActivityNotFoundException e)
						{
							e.printStackTrace();
						}
						(getActivity()).startActivityForResult(camIntent, ACTION_CAMERA);
						break;
					case 1:
						Intent galIntent = new Intent();
						galIntent.setType("image/*");
						galIntent.setAction(Intent.ACTION_GET_CONTENT);
						(getActivity()).startActivityForResult(galIntent, ACTION_GALLERY);
						break;
				}
			}
		});
		adb.show();
	}

	public void addImages(String... uriList)
	{
		if (uriList != null)
		{
			for (String string : uriList)
			{
				// Uri uri = Uri.parse(string);
				// addImage(uri);
				mUriList.add(string);
			}
		}
	}

	public String[] getUriList()
	{
		ArrayList<String> uriList = new ArrayList<String>();

		for (PictureItem item : mImageList)
		{
			uriList.add(item.uri.toString());
		}

		return uriList.toArray(new String[uriList.size()]);
	}

	public class PictureItem
	{
		public Bitmap image;
		public Uri uri;
	}

}
