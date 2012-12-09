package com.example.research;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

public class CreateAdsActivity extends FragmentActivity implements CreateAdsFragmentListener
{
	private int mFragmentId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_ads);

		if (findViewById(R.id.layoutContainer) != null)
		{
			if (savedInstanceState != null)
				return;

			CreateAdsDetailFragment detailFragment = new CreateAdsDetailFragment();

			detailFragment.setArguments(getIntent().getExtras());

			getSupportFragmentManager().beginTransaction().add(R.id.layoutContainer, detailFragment).commit();

			mFragmentId = detailFragment.getId();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_create_ads, menu);
		return true;
	}

	@Override
	public void onBack(Fragment fragment)
	{
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onNext(Fragment fragment)
	{
		if (fragment instanceof CreateAdsDetailFragment)
		{
			CreateAdsLoginFragment loginFragment = new CreateAdsLoginFragment();

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.layoutContainer, loginFragment);
			transaction.addToBackStack(null);
			transaction.commit();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		Fragment fragment = getSupportFragmentManager().findFragmentById(mFragmentId);
		if (fragment instanceof CreateAdsDetailFragment)
			fragment.onActivityResult(requestCode, resultCode, data);
	}

	public class CreateAdsItem
	{

	}
}
