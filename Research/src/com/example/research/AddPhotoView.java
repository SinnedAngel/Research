package com.example.research;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AddPhotoView extends LinearLayout
{
	private Context mContext;

	private LayoutParams mContainerParams;
	private LayoutParams mItemParams;

	private ImageButton mButtonAddPhoto;

	private LinearLayout mCurrentContainer;

	private int mItemSize = 80;
	private int mPadding = 5;
	private int mHalfPadding = 2;

	private int mMaxColumn = 3;
	private int mMaxItemCount = 0;

	private boolean mIsUseSize = false;

	private static final int ACTION_CAMERA = 1;
	private static final int ACTION_GALLERY = 2;

	private Uri mImageCaptureUri;

	private ArrayList<PictureItem> mImageList = new ArrayList<PictureItem>();

	private ArrayList<String> mUriList = new ArrayList<String>();

	private String mPicturePath;

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
		this.mMaxColumn = maxColumn;
		mIsUseSize = false;
	}

	public void setImageSize(int size)
	{
		mItemSize = size;
		mIsUseSize = true;
	}

	public void setMaxItemCount(int maxItemCount)
	{
		this.mMaxItemCount = maxItemCount;
	}

	public AddPhotoView(Context context)
	{
		super(context);
		initialize(context, null);
	}

	public AddPhotoView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize(context, attrs);
	}

	public AddPhotoView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize(context, attrs);
	}

	private void initialize(Context context, AttributeSet attrs)
	{
		mContext = context;

		mContainerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		setOrientation(VERTICAL);

		if (attrs != null)
		{
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AddPhotoView);

			mIsUseSize = a.getBoolean(R.styleable.AddPhotoView_isUseSize, false);
			mItemSize = (int) a.getDimension(R.styleable.AddPhotoView_itemSize, 80);
			mMaxColumn = a.getInteger(R.styleable.AddPhotoView_maxColumns, 3);
			mPicturePath = a.getString(R.styleable.AddPhotoView_picturePath);
			mMaxItemCount = a.getInteger(R.styleable.AddPhotoView_maxItemCount, 0);

			a.recycle();
		}

		mItemParams = new LayoutParams(mItemSize, mItemSize);
		// mItemParams.rightMargin = mPadding;
		mItemParams.topMargin = mPadding;
		mItemParams.leftMargin = mHalfPadding;
		mItemParams.rightMargin = mHalfPadding;

		mCurrentContainer = new LinearLayout(context);
		mCurrentContainer.setLayoutParams(mContainerParams);
		mCurrentContainer.setOrientation(LinearLayout.HORIZONTAL);

		// int padding = mPadding / 2;
		//
		// mCurrentContainer.setPadding(padding, padding, padding, padding);
		addView(mCurrentContainer);

		mButtonAddPhoto = new ImageButton(context);
		mButtonAddPhoto.setBackgroundResource(R.drawable.upload_foto_bg);
		mButtonAddPhoto.setImageResource(R.drawable.thumbnail_upload);
		mButtonAddPhoto.setLayoutParams(mItemParams);
		mCurrentContainer.addView(mButtonAddPhoto);

		mButtonAddPhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showPictureSelection();
			}
		});

	}

	private void showPictureSelection()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
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
						((Activity) mContext).startActivityForResult(camIntent, ACTION_CAMERA);
						break;
					case 1:
						Intent galIntent = new Intent();
						galIntent.setType("image/*");
						galIntent.setAction(Intent.ACTION_GET_CONTENT);
						((Activity) mContext).startActivityForResult(galIntent, ACTION_GALLERY);
						break;
				}
			}
		});
		adb.show();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int width = r - l;

		if (mIsUseSize)
			mMaxColumn = (width / mItemSize) - 1;
		else
			mItemSize = width / (mMaxColumn + 1);

		mPadding = mItemSize / mMaxColumn;
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
		// mCurrentContainer.setPadding(padding, padding, padding, padding);
		mContainerParams.leftMargin = mHalfPadding;
		mContainerParams.bottomMargin = mPadding;

		super.onLayout(changed, l, t, r, b);

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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ACTION_CAMERA)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Uri uri = mImageCaptureUri;
				addImage(uri);
			}
		}
		if (requestCode == ACTION_GALLERY)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				if (data != null)
				{
					Uri uri = data.getData();
					addImage(uri);
				}
			}
		}
	}

	private void addImage(Uri uri)
	{
		if (uri == null)
			return;

		InputStream inputStream = null;
		try
		{
			ContentResolver cr = mContext.getContentResolver();
			// Bitmap bitmap = Media.getBitmap(cr, uri);
			inputStream = cr.openInputStream(uri);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

			PictureItem item = new PictureItem();
			item.image = bitmap;
			item.uri = uri;

			mImageList.add(item);

			ImageView imageView = new ImageView(mContext);
			imageView.setImageBitmap(bitmap);
			imageView.setBackgroundResource(R.drawable.upload_foto_bg);
			imageView.setLayoutParams(mItemParams);

			int count = mCurrentContainer.getChildCount();
			if (count > 0)
				mCurrentContainer.addView(imageView, count - 1);

			if (mMaxItemCount > 0 && mImageList.size() >= mMaxItemCount)
				mButtonAddPhoto.setVisibility(GONE);
			else if (count + 1 > mMaxColumn)
			{
				mCurrentContainer.removeView(mButtonAddPhoto);

				mCurrentContainer = new LinearLayout(mContext);
				mCurrentContainer.setLayoutParams(mContainerParams);
				mCurrentContainer.setOrientation(LinearLayout.HORIZONTAL);
				// mCurrentContainer.setPadding(mPadding, mPadding, mPadding,
				// mPadding);
				addView(mCurrentContainer);

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
