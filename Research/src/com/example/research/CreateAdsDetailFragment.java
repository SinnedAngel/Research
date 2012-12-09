package com.example.research;

import java.util.ArrayList;

import com.example.research.adapter.CreateAdsAdapter;
import com.example.research.adapter.CreateAdsAdapter.AdsAdapterItem;
import com.example.research.data.DataProvider;
import com.example.research.data.TableCategory;
import com.example.research.lib.JsonWebService;
import com.example.research.lib.JsonWebService.JsonWebServiceListener;
import com.example.research.lib.JsonWebService.WebServiceData;
import com.example.research.lib.JsonWebService.WebServiceStatus;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class CreateAdsDetailFragment extends Fragment implements LoaderCallbacks<Cursor>, JsonWebServiceListener
{
	private String[] types = { "Tipe Iklan", "Dicari", "Dijual", "Disewakan", "Jasa" };
	private String[] conditions = { "Kondisi", "Baru", "Bekas" };

	private ArrayList<AdsAdapterItem> mCategoryList = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mSubcategoryList = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mSubcategory2List = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mRegionList = new ArrayList<AdsAdapterItem>();
	private ArrayList<AdsAdapterItem> mCityList = new ArrayList<AdsAdapterItem>();

	private ArrayList<Cursor> mCursorList = new ArrayList<Cursor>();

	private ArrayAdapter<String> mTypesAdapter, mConditionsAdapter;

	private CreateAdsAdapter mAdapterCategories, mAdapterSubcategories, mAdapterSubcategories2, mAdapterRegion,
			mCityAdapter;

	private Spinner mSpinnerTypes;
	private Spinner mSpinnerConditions;
	private Spinner mSpinnerCategories;
	private Spinner mSpinnerSubcategories;
	private Spinner mSpinnerSubcategories2;
	private Spinner mSpinnerRegion;
	private Spinner mSpinnerCities;

	private AddPhotoView mAddPhotoView;

	private int mCurrentCategory = 0;
	private int mCurrentLevel = 1;

	private LoaderManager mLoaderManager;
	private JsonWebService mRegionWebService;

	private static final int CURSOR_CATEGORIES = 1;
	private static final int CURSOR_SUBCATEGORIES = 2;
	private static final int CURSOR_SUBCATEGORIES2 = 3;
	private static final int CURSOR_REGIONS = 4;

	private CreateAdsFragmentListener mListener;

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null && mAddPhotoView != null)
			mAddPhotoView.addImages(savedInstanceState.getStringArray("IMAGES"));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		FragmentActivity activity = getActivity();

		View v = inflater.inflate(R.layout.create_ads_detail_fragment, container, false);

		mAddPhotoView = (AddPhotoView) v.findViewById(R.id.addPhotoView);

		mSpinnerTypes = (Spinner) v.findViewById(R.id.spinner_create_ads_types);
		mTypesAdapter = new ArrayAdapter<String>(activity, R.layout.create_ads_spinner_dropdown_item,
				android.R.id.text1, types);
		mSpinnerTypes.setAdapter(mTypesAdapter);

		mSpinnerConditions = (Spinner) v.findViewById(R.id.spinner_create_ads_conditions);
		mConditionsAdapter = new ArrayAdapter<String>(activity, R.layout.create_ads_spinner_dropdown_item,
				android.R.id.text1, conditions);
		mSpinnerConditions.setAdapter(mConditionsAdapter);

		mAdapterCategories = new CreateAdsAdapter(activity, R.layout.create_ads_spinner_dropdown_item, mCategoryList);
		mSpinnerCategories = (Spinner) v.findViewById(R.id.spinner_create_ads_categories);
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
						mLoaderManager.restartLoader(CURSOR_SUBCATEGORIES, null, CreateAdsDetailFragment.this);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		mAdapterSubcategories = new CreateAdsAdapter(activity, R.layout.create_ads_spinner_dropdown_item,
				mSubcategoryList);
		mSpinnerSubcategories = (Spinner) v.findViewById(R.id.spinner_create_ads_subcategories);
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
						mLoaderManager.restartLoader(CURSOR_SUBCATEGORIES2, null, CreateAdsDetailFragment.this);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		mAdapterSubcategories2 = new CreateAdsAdapter(activity, R.layout.create_ads_spinner_dropdown_item,
				mSubcategory2List);
		mSpinnerSubcategories2 = (Spinner) v.findViewById(R.id.spinner_create_ads_subcategories_2);
		mSpinnerSubcategories2.setAdapter(mAdapterSubcategories2);

		mAdapterRegion = new CreateAdsAdapter(activity, R.layout.create_ads_spinner_dropdown_item, mRegionList);
		mSpinnerRegion = (Spinner) v.findViewById(R.id.spinner_create_ads_provinces);
		mSpinnerRegion.setAdapter(mAdapterRegion);
		mSpinnerRegion.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
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

		mCityAdapter = new CreateAdsAdapter(activity, R.layout.create_ads_spinner_dropdown_item, mCityList);
		mSpinnerCities = (Spinner) v.findViewById(R.id.spinner_create_ads_cities);
		mSpinnerCities.setAdapter(mCityAdapter);

		Button buttonContinue = (Button) v.findViewById(R.id.btn_create_ads_continue);
		buttonContinue.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mListener != null)// && isInputValid())
					mListener.onNext(CreateAdsDetailFragment.this);
			}
		});

		Button buttonCancel = (Button) v.findViewById(R.id.btn_create_ads_cancel);
		buttonCancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mListener != null)
					mListener.onBack(CreateAdsDetailFragment.this);
			}
		});

		mLoaderManager = activity.getSupportLoaderManager();
		if (mLoaderManager != null)
		{
			mLoaderManager.initLoader(CURSOR_CATEGORIES, null, this);
			mLoaderManager.initLoader(CURSOR_REGIONS, null, this);
		}

		return v;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		if (activity instanceof CreateAdsFragmentListener)
			mListener = (CreateAdsFragmentListener) activity;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putStringArray("IMAGES", mAddPhotoView.getUriList());
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		if (id == CURSOR_REGIONS)
		{
			String[] projection = { TableCategory.ID, TableCategory.CODE, TableCategory.TITLE };
			String selection = null;
			String[] selectionArgs = null;

			return new CursorLoader(getActivity(), DataProvider.CONTENT_URI_REG, projection, selection, selectionArgs,
					"_id asc");
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
			return new CursorLoader(getActivity(), DataProvider.CONTENT_URI_CAT, projection, selection, selectionArgs,
					"title asc");
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
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (mAddPhotoView != null)
			mAddPhotoView.onActivityResult(requestCode, resultCode, data);
	}

	private void requestCities(int regionCode)
	{
		if (mRegionWebService != null)
			mRegionWebService.cancel();

		mRegionWebService = new JsonWebService();
		mRegionWebService.setListener(this);
		mRegionWebService.openAsync("http://api.tokobagus.com/v1/region.residence/region/" + regionCode);
	}

	private boolean isInputValid()
	{
		EditText editTextTitle = (EditText) getView().findViewById(R.id.et_create_ads_title);
		if (editTextTitle.length() < 6)
			return false;

		if (mSpinnerTypes.getSelectedItemPosition() < 1)
			return false;

		if (mSpinnerConditions.getSelectedItemPosition() < 1)
			return false;

		EditText editTextPrice = (EditText) getView().findViewById(R.id.et_create_ads_price);
		if (editTextPrice.length() < 1)
			return false;

		if (mSpinnerCategories.getSelectedItemPosition() < 1)
			return false;

		if (mSpinnerSubcategories.getSelectedItemPosition() < 1)
			return false;

		if (mSpinnerSubcategories2.getVisibility() == View.VISIBLE
				&& mSpinnerSubcategories2.getSelectedItemPosition() < 1)
			return false;

		if (mSpinnerRegion.getSelectedItemPosition() < 1)
			return false;

		if (mSpinnerCities.getVisibility() == View.VISIBLE && mSpinnerCities.getSelectedItemPosition() < 1)
			return false;

		return true;
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

}
