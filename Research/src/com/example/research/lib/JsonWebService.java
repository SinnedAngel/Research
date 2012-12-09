package com.example.research.lib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.research.lib.HttpConnection.HttpConnectionResult;

import android.os.AsyncTask;

public class JsonWebService
{
	private HttpConnection mConnection;

	private AsyncTask<String, WebServiceData, WebServiceStatus> mTask;

	private JsonWebServiceListener mListener;

	private ArrayList<BasicNameValuePair> mParams;
	private HttpEntity mEntity;

	public void setListener(JsonWebServiceListener listener)
	{
		this.mListener = listener;
	}

	public boolean addParams(String name, String value)
	{
		if (mParams == null)
			mParams = new ArrayList<BasicNameValuePair>();

		mParams.add(new BasicNameValuePair(name, value));

		try
		{
			if (mEntity == null)
			{
				mEntity = new UrlEncodedFormEntity(mParams);
				return true;
			}
		}
		catch (UnsupportedEncodingException e)
		{
		}
		return false;
	}

	public boolean setData(byte[] data)
	{
		if (mEntity == null)
		{
			mEntity = new ByteArrayEntity(data);
			return true;
		}
		return false;
	}

	public boolean setData(File data)
	{
		if (mEntity == null)
		{
			mEntity = new FileEntity(data, "image/jpeg");
			return true;
		}
		return false;
	}

	public boolean setData(String data)
	{
		if (mEntity == null)
		{
			try
			{
				mEntity = new StringEntity(data, HTTP.UTF_8);
			}
			catch (UnsupportedEncodingException e)
			{
			}
			return true;
		}
		return false;
	}

	public void openAsync(String url)
	{
		if (mTask != null)
			mTask.cancel(true);

		mTask = new AsyncTask<String, WebServiceData, WebServiceStatus>()
		{
			private Exception mError;

			@Override
			protected WebServiceStatus doInBackground(String... params)
			{
				WebServiceStatus status = new WebServiceStatus();
				int result = 0;

				if (params != null && params.length > 0)
				{
					InputStream stream = null;
					ByteArrayOutputStream baos = null;
					HttpConnectionResult connectionResult = null;

					mConnection = new HttpConnection();

					try
					{
						if (mParams == null)
							connectionResult = mConnection.open(params[0]);
						else
						{
							if (mEntity != null)
								connectionResult = mConnection.post(params[0], mEntity);
						}

						stream = connectionResult.getStatusStream();

						baos = new ByteArrayOutputStream();

						int size = 1024;
						byte[] buffer = new byte[size];
						while (size > 0)
						{
							size = stream.read(buffer, 0, 1024);
							if (size > 0)
								baos.write(buffer, 0, size);
						}

						String resultString = baos.toString();
						baos.close();

						result = parseAsync(resultString);
						status.mStatus = result;
					}
					catch (MalformedURLException e)
					{
						// if (mListener != null)
						// mListener.onError(e);
						mError = e;
					}
					catch (IOException e)
					{
						// if (mListener != null)
						// mListener.onError(e);
						mError = e;
					}
					catch (Exception e)
					{
						// if (mListener != null)
						// mListener.onError(e);
						mError = e;
					}
					finally
					{
						try
						{
							if (connectionResult != null)
								connectionResult.close();
						}
						catch (IOException e)
						{
						}

						try
						{
							if (baos != null)
								baos.close();
						}
						catch (IOException e)
						{
						}
					}
				}

				return status;
			}

			private int parseAsync(String jsonString)
			{
				int status = 0;
				try
				{
					JSONObject object = new JSONObject(jsonString);

					status = object.optInt("status", 0);

					JSONArray docs = object.optJSONArray("docs");

					if (docs != null)
					{
						int count = docs.length();
						for (int i = 0; i < count; i++)
						{
							JSONObject doc = docs.optJSONObject(i);

							if (doc != null && mListener != null)
							{
								WebServiceData data = mListener.createData();
								if (data != null)
								{
									data.mRawData = doc;
									data.parseData();

									publishProgress(data);
								}
							}
						}
					}
				}
				catch (JSONException e)
				{
					// if (mListener != null)
					// mListener.onError(e);
					mError = e;
				}

				return status;
			}

			@Override
			protected void onProgressUpdate(WebServiceData... values)
			{
				if (values != null && values.length > 0)
				{
					if (mListener != null)
						mListener.onProgress(values);
				}
			}

			@Override
			protected void onPostExecute(WebServiceStatus result)
			{
				if (mListener != null)
				{
					if (mError != null)
						mListener.onError(mError);
					else
						mListener.onResultReceived(result);
				}
			}

			@Override
			protected void onCancelled()
			{
				if (mConnection != null)
					mConnection.cancel();

				super.onCancelled();
			}
		};

		mTask.execute(url);
	}

	public void cancel()
	{
		if (mTask != null)
			mTask.cancel(true);
	}

	public static class WebServiceData
	{
		private JSONObject mRawData = null;

		public void parseData()
		{

		}

		protected String getString(String key)
		{
			return mRawData.optString(key);
		}

		protected int getInt(String key, int def)
		{
			return mRawData.optInt(key, def);
		}

		protected int getInt(String key)
		{
			return getInt(key, -1);
		}

		protected double getDouble(String key, double def)
		{
			return mRawData.optDouble(key, def);
		}

		protected double getDouble(String key)
		{
			return getDouble(key, -1);
		}

		protected boolean isTrue(String key, boolean def)
		{
			if (mRawData.has(key))
			{
				String result = mRawData.optString(key);

				try
				{
					return Boolean.parseBoolean(result);
				}
				catch (Exception e)
				{
					if ("1".equals(result))
						return true;
					else
						return false;
				}
			}

			return def;
		}

		protected boolean isTrue(String key)
		{
			return isTrue(key, false);
		}
	}

	public class WebServiceStatus
	{
		public static final int STATUS_FAILED = 0;
		public static final int STATUS_SUCCESS = 1;
		public static final int STATUS_MODERATED = 2;

		private int mStatus = 0;;

		public int getStatus()
		{
			return mStatus;
		}
	}

	public interface JsonWebServiceListener
	{
		public WebServiceData createData();

		public void onProgress(WebServiceData... data);

		public void onResultReceived(WebServiceStatus result);

		public void onError(Exception e);
	}
}
