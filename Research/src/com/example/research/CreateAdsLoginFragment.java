package com.example.research;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class CreateAdsLoginFragment extends Fragment
{
	private Button mButtonLogin;
	private EditText mEditTextUsername;
	private EditText mEditTextEmail;
	private EditText mEditTextPhone;
	private CheckBox mCheckBoxAgreement;
	private Button mButtonBack;
	private Button mButtonContinue;

	private CreateAdsFragmentListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.create_ads_login_fragment, container, false);

		mButtonLogin = (Button) v.findViewById(R.id.btn_create_ads_login);
		mEditTextUsername = (EditText) v.findViewById(R.id.et_create_ads_create_user_name);
		mEditTextEmail = (EditText) v.findViewById(R.id.et_create_ads_create_user_email);
		mEditTextPhone = (EditText) v.findViewById(R.id.et_create_ads_create_user_phone);

		mButtonBack = (Button) v.findViewById(R.id.btn_create_ads_login_back);
		mButtonBack.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mListener != null)
					mListener.onBack(CreateAdsLoginFragment.this);
			}
		});

		mButtonContinue = (Button) v.findViewById(R.id.btn_create_ads_login_continue);
		mButtonContinue.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mListener != null && isInputValid())
					mListener.onNext(CreateAdsLoginFragment.this);
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		if (activity instanceof CreateAdsFragmentListener)
			mListener = (CreateAdsFragmentListener) activity;
	}

	private boolean isInputValid()
	{
		if (mEditTextUsername.length() < 5)
			return false;

		if (mEditTextEmail.length() < 5)
			return false;

		if (mEditTextPhone.length() < 5)
			return false;

		return true;
	}
}
