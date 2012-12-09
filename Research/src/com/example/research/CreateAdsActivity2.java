package com.example.research;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.example.research.adapter.CreateAdsAdapter;
import com.example.research.adapter.CreateAdsAdapter.AdsAdapterItem;
import com.example.research.data.DataProvider;
import com.example.research.data.TableCategory;
import com.example.research.lib.JsonWebService;
import com.example.research.lib.JsonWebService.JsonWebServiceListener;
import com.example.research.lib.JsonWebService.WebServiceData;
import com.example.research.lib.JsonWebService.WebServiceStatus;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class CreateAdsActivity2 extends FragmentActivity implements LoaderCallbacks<Cursor>, JsonWebServiceListener
{
	private CreateAdsAdapter mAdapterCategories, mAdapterSubcategories, mAdapterSubcategories2, mAdapterRegion,
			mCityAdapter;

	private String[] types = { "Tipe Iklan", "Dicari", "Dijual", "Disewakan", "Jasa" };
	private String[] conditions = { "Kondisi", "Baru", "Bekas" };

	private Spinner mSpinnerCategories;
	private Spinner mSpinnerSubcategories;
	private Spinner mSpinnerSubcategories2;
	private Spinner mSpinnerCities;

	private LinearLayout mLayoutAddPictures;
	private ImageButton mButtonAddPictures;

	private ArrayAdapter<String> mTypesAdapter, mConditionsAdapter;

	private ArrayList<AdsAdapterItem> mCategoryList = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mSubcategoryList = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mSubcategory2List = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mRegionList = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mCityList = new ArrayList<AdsAdapterItem>();

	private int mCurrentCategory = 0;
	private int mCurrentLevel = 1;

	private static final int CURSOR_CATEGORIES = 1;
	private static final int CURSOR_SUBCATEGORIES = 2;
	private static final int CURSOR_SUBCATEGORIES2 = 3;
	private static final int CURSOR_REGIONS = 4;

	private static final int PICTURE_FROM_CAMERA = 1;
	private static final int PICTURE_FROM_GALLERY = 2;

	private LoaderManager mLoaderManager;
	private JsonWebService mRegionWebService;

	private Uri mImageCaptureUri;

	private ArrayList<Cursor> mCursorList = new ArrayList<Cursor>();

	private ArrayList<PictureItem> mImageList = new ArrayList<PictureItem>();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_ads_2);

		Spinner spinnerTypes = (Spinner) findViewById(R.id.spinner_create_ads_types);
		mTypesAdapter = new ArrayAdapter<String>(this, R.layout.create_ads_spinner_dropdown_item, android.R.id.text1,
				types);
		spinnerTypes.setAdapter(mTypesAdapter);

		Spinner spinnerConditions = (Spinner) findViewById(R.id.spinner_create_ads_conditions);
		mConditionsAdapter = new ArrayAdapter<String>(this, R.layout.create_ads_spinner_dropdown_item,
				android.R.id.text1, conditions);
		spinnerConditions.setAdapter(mConditionsAdapter);

		mAdapterCategories = new CreateAdsAdapter(this, R.layout.create_ads_spinner_dropdown_item, mCategoryList);
		mSpinnerCategories = (Spinner) findViewById(R.id.spinner_create_ads_categories);
		mSpinnerCategories.setAdapter(mAdapterCategories);
		mSpinnerCategories.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (position > 0 && mCategoryList.size() > position)
				{
					AdsAdapterItem item = mCategoryList.get(position);
					mCurrentCategory = item.key;
					mCurrentLevel = 2;

					if (mLoaderManager != null)
						mLoaderManager.restartLoader(CURSOR_SUBCATEGORIES, null, CreateAdsActivity2.this);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		mAdapterSubcategories = new CreateAdsAdapter(this, R.layout.create_ads_spinner_dropdown_item, mSubcategoryList);
		mSpinnerSubcategories = (Spinner) findViewById(R.id.spinner_create_ads_subcategories);
		mSpinnerSubcategories.setAdapter(mAdapterSubcategories);
		mSpinnerSubcategories.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (position > 0 && mSubcategoryList.size() > position)
				{
					AdsAdapterItem item = mSubcategoryList.get(position);
					mCurrentCategory = item.key;
					mCurrentLevel = 3;

					if (mLoaderManager != null)
						mLoaderManager.restartLoader(CURSOR_SUBCATEGORIES2, null, CreateAdsActivity2.this);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		mAdapterSubcategories2 = new CreateAdsAdapter(this, R.layout.create_ads_spinner_dropdown_item,
				mSubcategory2List);
		mSpinnerSubcategories2 = (Spinner) findViewById(R.id.spinner_create_ads_subcategories_2);
		mSpinnerSubcategories2.setAdapter(mAdapterSubcategories2);

		mAdapterRegion = new CreateAdsAdapter(this, R.layout.create_ads_spinner_dropdown_item, mRegionList);
		Spinner spinnerRegion = (Spinner) findViewById(R.id.spinner_create_ads_provinces);
		spinnerRegion.setAdapter(mAdapterRegion);
		spinnerRegion.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (position > 0 && mRegionList.size() > position)
				{
					AdsAdapterItem item = mRegionList.get(position);

					mCityList.clear();

					requestCities(item.key);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		mCityAdapter = new CreateAdsAdapter(this, R.layout.create_ads_spinner_dropdown_item, mCityList);
		mSpinnerCities = (Spinner) findViewById(R.id.spinner_create_ads_cities);
		mSpinnerCities.setAdapter(mCityAdapter);

		Button buttonContinue = (Button) findViewById(R.id.btn_create_ads_continue);
		buttonContinue.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// JsonWebService lostPassword = new JsonWebService();
				// lostPassword.setListener(new JsonWebServiceListener()
				// {
				// @Override
				// public void onResultReceived(WebServiceStatus result)
				// {
				// if (result != null)
				// {
				// String status = "FAILED";
				//
				// if (result.getStatus() == WebServiceStatus.STATUS_SUCCESS)
				// status = "SUCCESS";
				//
				// Toast.makeText(CreateAdsActivity.this, status,
				// Toast.LENGTH_LONG).show();
				// }
				// }
				//
				// @Override
				// public void onProgress(WebServiceData... data)
				// {
				// if (data != null && data.length > 0)
				// {
				//
				// }
				// }
				//
				// @Override
				// public void onError(Exception e)
				// {
				// if (e != null)
				// {
				// Toast.makeText(CreateAdsActivity.this, e.getMessage(),
				// Toast.LENGTH_LONG).show();
				// }
				// }
				//
				// @Override
				// public WebServiceData createData()
				// {
				// return new WebServiceData();
				// }
				// });
				// lostPassword.addParams("email",
				// "tester.tokobagus.com@gmail.com");
				// lostPassword.openAsync("http://api.tokobagus.net/v1/auth.recover");
			}
		});

		mLayoutAddPictures = (LinearLayout) findViewById(R.id.layout_create_ads_pictures);

		mButtonAddPictures = (ImageButton) findViewById(R.id.btn_create_ads_pictures);
		mButtonAddPictures.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showAddPictureSelection();
			}
		});

		mLoaderManager = getSupportLoaderManager();
		if (mLoaderManager != null)
		{
			mLoaderManager.initLoader(CURSOR_CATEGORIES, null, this);
			mLoaderManager.initLoader(CURSOR_REGIONS, null, this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		ArrayList<String> uriList = new ArrayList<String>();
		for (PictureItem item : mImageList)
		{

		}
	}

	private void requestCities(int regionCode)
	{
		if (mRegionWebService != null)
			mRegionWebService.cancel();

		mRegionWebService = new JsonWebService();
		mRegionWebService.setListener(this);
		mRegionWebService.openAsync("http://api.tokobagus.com/v1/region.residence/region/" + regionCode);
	}

	private void showAddPictureSelection()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Pick a Source");
		adb.setItems(new String[] { "Camera", "Gallery" }, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
					case 0:

						Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

						mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
								"tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
						camIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

						try
						{
							camIntent.putExtra("return-data", true);
						}
						catch (ActivityNotFoundException e)
						{
							e.printStackTrace();
						}
						startActivityForResult(camIntent, PICTURE_FROM_CAMERA);
						break;
					case 1:
						Intent galIntent = new Intent();
						galIntent.setType("image/*");
						galIntent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(galIntent, PICTURE_FROM_GALLERY);
						break;
				}
			}
		});
		adb.show();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		if (id == CURSOR_REGIONS)
		{
			String[] projection = { TableCategory.ID, TableCategory.CODE, TableCategory.TITLE };
			String selection = null;
			String[] selectionArgs = null;

			return new CursorLoader(this, DataProvider.CONTENT_URI_REG, projection, selection, selectionArgs, "_id asc");
		}
		else
		{
			String[] projection = { TableCategory.ID, TableCategory.CODE, TableCategory.TITLE, TableCategory.LEVEL };
			String selection = null;
			String[] selectionArgs = { "" };
			if (0 < mCurrentCategory)
			{
				selection = TableCategory.PARENT + "=?";
				selectionArgs = new String[] { String.valueOf(mCurrentCategory) };

				if (mCurrentLevel == 2)
					mSubcategoryList.clear();
				else
					mSubcategory2List.clear();
			}
			else
			{
				selection = TableCategory.LEVEL + "=?";
				selectionArgs = new String[] { "1" };
			}
			return new CursorLoader(this, DataProvider.CONTENT_URI_CAT, projection, selection, selectionArgs,
					"title asc");
		}
	}

	private void addImage(Uri uri)
	{
		if (uri == null)
			return;

		// InputStream inputStream = null;
		try
		{
			ContentResolver cr = getContentResolver();
			// // inputStream = cr.openInputStream(uri);
			// BitmapFactory.Options options = new BitmapFactory.Options();
			// options.inSampleSize = 8;
			// Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

			Bitmap bitmap = Media.getBitmap(cr, uri);

			PictureItem item = new PictureItem();
			item.image = bitmap;
			item.uri = uri;

			mImageList.add(item);

			ImageView imageView = new ImageView(this);
			imageView.setImageBitmap(bitmap);
			imageView.setBackgroundResource(R.drawable.upload_foto_bg);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mButtonAddPictures.getWidth(),
					mButtonAddPictures.getHeight());
			imageView.setLayoutParams(params);

			int count = mLayoutAddPictures.getChildCount();
			if (count > 0)
			{
				mLayoutAddPictures.addView(imageView, count - 1);
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

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		if (!mCursorList.contains(data))
			mCursorList.add(data);

		int id = loader.getId();

		if (id == CURSOR_REGIONS)
		{
			if (data.moveToFirst())
			{
				int index = -1;
				do
				{
					AdsAdapterItem item = new AdsAdapterItem();

					index = data.getColumnIndex(TableCategory.CODE);
					item.key = data.getInt(index);

					index = data.getColumnIndex(TableCategory.TITLE);
					item.title = data.getString(index);

					mAdapterRegion.add(item);
				} while (data.moveToNext());

				mAdapterRegion.notifyDataSetChanged();
			}
		}
		else
		{
			if (0 < mCurrentCategory)
			{
				AdsAdapterItem firstItem = new AdsAdapterItem();
				firstItem.key = 0;
				firstItem.title = getString(R.string.spinner_create_ads_subcategories_prompt);

				if (mCurrentLevel == 2)
				{
					mSubcategoryList.add(firstItem);

					if (data.moveToFirst())
					{
						int index = -1;
						do
						{
							AdsAdapterItem item = new AdsAdapterItem();

							index = data.getColumnIndex(TableCategory.CODE);
							item.key = data.getInt(index);

							index = data.getColumnIndex(TableCategory.TITLE);
							item.title = data.getString(index);

							mSubcategoryList.add(item);
						} while (data.moveToNext());

						mAdapterSubcategories.notifyDataSetChanged();
						mSpinnerSubcategories.setVisibility(View.VISIBLE);
						mSpinnerSubcategories.setSelection(0);
					}
					else
						mSpinnerSubcategories.setVisibility(View.GONE);

					mAdapterSubcategories2.notifyDataSetChanged();
					mSpinnerSubcategories2.setVisibility(View.GONE);
					mSpinnerSubcategories2.setSelection(0);
				}
				else
				{
					mSubcategory2List.add(firstItem);

					if (data.moveToFirst())
					{
						int index = -1;
						do
						{
							AdsAdapterItem item = new AdsAdapterItem();

							index = data.getColumnIndex(TableCategory.CODE);
							item.key = data.getInt(index);

							index = data.getColumnIndex(TableCategory.TITLE);
							item.title = data.getString(index);

							mSubcategory2List.add(item);
						} while (data.moveToNext());

						mAdapterSubcategories2.notifyDataSetChanged();
						mSpinnerSubcategories2.setVisibility(View.VISIBLE);
						mSpinnerSubcategories2.setSelection(0);
					}
					else
						mSpinnerSubcategories2.setVisibility(View.GONE);
				}

			}
			else
			{
				AdsAdapterItem firstItem = new AdsAdapterItem();
				firstItem.key = 0;
				firstItem.title = getString(R.string.spinner_create_ads_categories_prompt);

				mCategoryList.add(firstItem);

				if (data.moveToFirst())
				{
					int index = -1;
					do
					{
						AdsAdapterItem item = new AdsAdapterItem();

						index = data.getColumnIndex(TableCategory.CODE);
						item.key = data.getInt(index);

						index = data.getColumnIndex(TableCategory.TITLE);
						item.title = data.getString(index);

						mCategoryList.add(item);
					} while (data.moveToNext());
				}

				mAdapterCategories.notifyDataSetChanged();

				mSpinnerSubcategories.setVisibility(View.GONE);
				mSpinnerSubcategories2.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		for (Cursor cursor : mCursorList)
		{
			cursor.close();
		}
	}

	@Override
	public WebServiceData createData()
	{
		return new CityItem();
	}

	@Override
	public void onProgress(WebServiceData... data)
	{
		if (data != null && data.length > 0)
		{
			for (WebServiceData webServiceData : data)
			{
				if (webServiceData instanceof AdsAdapterItem)
				{
					mCityList.add((AdsAdapterItem) webServiceData);
				}
			}

			mCityAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onResultReceived(WebServiceStatus result)
	{
		if (result != null && result.getStatus() == WebServiceStatus.STATUS_SUCCESS && mCityList.size() > 0)
			mSpinnerCities.setVisibility(View.VISIBLE);
		else
			mSpinnerCities.setVisibility(View.GONE);
	}

	@Override
	public void onError(Exception e)
	{
		if (e != null)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public class CityItem extends AdsAdapterItem
	{
		@Override
		public void parseData()
		{
			key = getInt("kab_id", -1);
			title = getString("kab_name");
		}
	}

	public class PictureItem
	{
		public Bitmap image;
		public Uri uri;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == PICTURE_FROM_CAMERA)
		{
			if (resultCode == RESULT_OK)
			{
				Uri uri = mImageCaptureUri;
				addImage(uri);
			}
		}
		if (requestCode == PICTURE_FROM_GALLERY)
		{
			if (resultCode == RESULT_OK)
			{
				if (data != null)
				{
					Uri uri = data.getData();
					addImage(uri);
				}
			}
		}
	}
}
