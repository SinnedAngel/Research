package com.example.research.lib;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import android.os.AsyncTask;

public class HttpConnection
{
	private AsyncTask<String, HttpConnectionResult, Integer> mTask;

	private HttpConnectionListener mListener;

	public void setListener(HttpConnectionListener listener)
	{
		this.mListener = listener;
	}

	// Not done yet
	public void openAync(String url)
	{
		if (mTask != null)
			mTask.cancel(true);

		mTask = new AsyncTask<String, HttpConnection.HttpConnectionResult, Integer>()
		{
			@Override
			protected Integer doInBackground(String... params)
			{
				if (params != null && params.length > 0)
				{
					HttpConnectionResult result = null;
					try
					{
						if (isCancelled())
							return -1;

						result = open(params[0]);
						if (isCancelled())
							return -1;

						if (mListener != null)
							mListener.onResultReceived(result);
					}
					catch (MalformedURLException e)
					{
						if (mListener != null)
							mListener.onError(e);
					}
					catch (IOException e)
					{
						if (mListener != null)
							mListener.onError(e);
					}
					catch (Exception e)
					{
						if (mListener != null)
							mListener.onError(e);
					}
					finally
					{
						try
						{
							if (result != null)
								result.close();
						}
						catch (IOException e)
						{
						}
					}
				}
				return 0;
			}

			public HttpConnectionResult open(String url) throws MalformedURLException, IOException
			{
				HttpURLConnection connection = null;

				HttpConnectionResult result = new HttpConnectionResult();

				URL urlAddress = new URL(url);

				if (isCancelled())
					return null;
				connection = (HttpURLConnection) urlAddress.openConnection();
				if (isCancelled())
					return null;
				connection.connect();
				if (isCancelled())
					return null;

				result.mStatusCode = connection.getResponseCode();
				result.mStatusMessage = connection.getResponseMessage();

				if (result.mStatusCode == HttpsURLConnection.HTTP_OK)
				{
					if (isCancelled())
						return null;
					result.mInputStream = connection.getInputStream();
				}
				return result;
			}
		};

		mTask.execute(url);
	}

	public HttpConnectionResult open(String url) throws MalformedURLException, IOException
	{
		HttpURLConnection connection = null;

		HttpConnectionResult result = new HttpConnectionResult();

		URL urlAddress = new URL(url);

		connection = (HttpURLConnection) urlAddress.openConnection();
		connection.connect();

		result.mStatusCode = connection.getResponseCode();
		result.mStatusMessage = connection.getResponseMessage();

		if (result.mStatusCode == HttpsURLConnection.HTTP_OK)
		{
			result.mInputStream = connection.getInputStream();
		}

		return result;
	}

	public HttpConnectionResult post(String url, HttpEntity entity) throws MalformedURLException, IOException
	{
		HttpConnectionResult result = new HttpConnectionResult();

		HttpURLConnection connection = null;

		URL urlAddress = new URL(url);

		connection = (HttpURLConnection) urlAddress.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");

		// String stringParams = "";
		// if (params != null)
		// {
		// for (BasicNameValuePair basicNameValuePair : params)
		// {
		// if (stringParams.length() > 0)
		// stringParams += "&";
		// stringParams += basicNameValuePair.getName() + "=" +
		// basicNameValuePair.getValue();
		// }
		// }

		connection.connect();

		OutputStream outputStream = null;

		outputStream = connection.getOutputStream();
		if (outputStream != null)
		{
			// outputStream.write(stringParams.getBytes());
			entity.writeTo(outputStream);
		}

		result.mStatusCode = connection.getResponseCode();
		result.mStatusMessage = connection.getResponseMessage();

		if (result.mStatusCode == HttpsURLConnection.HTTP_OK)
		{
			result.mInputStream = connection.getInputStream();
		}

		return result;
	}

	public void cancel()
	{
		if (mTask != null)
			mTask.cancel(true);
	}

	public class HttpConnectionResult implements Closeable
	{
		private int mStatusCode = -1;
		private String mStatusMessage;
		private InputStream mInputStream;

		private HttpURLConnection mConnection;

		public int getStatusCode()
		{
			return mStatusCode;
		}

		public String getStatusMessage()
		{
			return mStatusMessage;
		}

		public InputStream getStatusStream()
		{
			return mInputStream;
		}

		@Override
		public void close() throws IOException
		{
			if (mConnection != null)
				mConnection.disconnect();

			if (mInputStream != null)
				mInputStream.close();
		}
	}

	public interface HttpConnectionListener
	{
		public void onResultReceived(HttpConnectionResult result);

		public void onError(Exception e);
	}
}
