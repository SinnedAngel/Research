package com.example.research.adapter;

import java.util.List;

import com.example.research.R;
import com.example.research.lib.JsonWebService.WebServiceData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CreateAdsAdapter extends ArrayAdapter<CreateAdsAdapter.AdsAdapterItem>
{
	private LayoutInflater mInflater;

	public CreateAdsAdapter(Context context, int textViewResourceId, List<AdsAdapterItem> objects)
	{
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		TextView textViewHolder = null;

		if (convertView == null)
			convertView = mInflater.inflate(R.layout.create_ads_spinner_dropdown_item, null);
		else
		{
			Object tag = convertView.getTag();
			if (tag instanceof TextView)
				textViewHolder = (TextView) tag;
		}

		if (textViewHolder == null)
		{
			textViewHolder = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(textViewHolder);
		}

		AdsAdapterItem item = getItem(position);
		textViewHolder.setText(item.title);

		return convertView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		TextView textViewHolder = null;

		if (convertView == null)
			convertView = mInflater.inflate(R.layout.create_ads_spinner_dropdown_item, null);
		else
		{
			Object tag = convertView.getTag();
			if (tag instanceof TextView)
				textViewHolder = (TextView) tag;
		}

		if (textViewHolder == null)
		{
			textViewHolder = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(textViewHolder);
		}

		AdsAdapterItem item = getItem(position);
		textViewHolder.setText(item.title);

		return convertView;
	}

	public static class AdsAdapterItem extends WebServiceData
	{
		public int key;
		public String title;
	}
}
