package de.bht.bachelor.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import de.beutch.bachelorwork.util.file.Path;
import de.bht.bachelor.helper.NetworkHelper;
import de.bht.bachelor.message.ServiceMessenger;

/**
 * 
 * @author and
 * 
 * @param URL
 *            to download file
 * @param progress
 *            value
 * @param absolute
 *            path to the successfully downloaded file
 * 
 */
public class DownloadOcrTraineddata extends AsyncTask<URL, String, String> {

	/**
	 * 
	 * @param downloadFileName
	 *            name of the tessdata file
	 * @param uncompressFileName
	 *            name of uncompressed file, if null set
	 * @param
	 */
	public DownloadOcrTraineddata(String downloadFileName, String uncompressFileName, Handler handler) {
		Log.d(TAG, "creating new Download task with downloadFileName: " + downloadFileName + ",decompressFileName: " + uncompressFileName);

		this.downloadFileName = downloadFileName;
		this.uncompressFileName = uncompressFileName;
		this.handler = handler;
		serviceMessenger = new ServiceMessenger(this.handler);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		serviceMessenger.sendMessage(DIALOG_DOWNLOAD_PROGRESS, 6, this);

	}

	@Override
	protected String doInBackground(URL... arg0) {
		// TODO Auto-generated method stub
		int count;
		InputStream input;
		OutputStream output;
		String absolutePath = null;

		try {
			URL url = arg0[0];
			URLConnection conexion = url.openConnection();
			conexion.connect();

			int lenghtOfFile = conexion.getContentLength();
			Log.d(TAG, "Lenght of file: " + lenghtOfFile);
			absolutePath = Path.LANGUAGE_TRAINEDDATA_DIR + this.downloadFileName;

			input = new BufferedInputStream(url.openStream());
			output = new FileOutputStream(absolutePath);

			byte data[] = new byte[1024];

			long total = 0;

			while ((count = input.read(data)) != -1) {
				total += count;
				if (mProgressDialog != null)
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
		} catch (UnknownHostException e) {
			Log.e(TAG, "Can not connect to host : " + arg0[0].toString());
			serviceMessenger.sendMessage(NetworkHelper.NO_INTERNET_CONECTION, 6, "Keine Verbindung, versuchen Sie seine WLAN oder ROAMING neu starten");
		} catch (Exception e) {
			Log.e(TAG, "Could not downlaod tessdata!", e);
		}
		return absolutePath;
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		mProgressDialog.setProgress(Integer.parseInt(progress[0]));
	}

	@Override
	protected void onPostExecute(String absolutePath) {
		OutputStream out;
		GZIPInputStream gzipInputStream = null;

		if (absolutePath == null) {
			serviceMessenger.sendMessage(0, 6, new Exception("Something went wrong, no path to downloaded file !"));
			return;
		}

		if (!new File(absolutePath).exists()) {
			Log.e(TAG, "Downloaded file with absolutePath: " + absolutePath + " is not existing");
			return;
		}

		if (uncompressFileName == null)
			return;
		else
			Log.d(TAG, "file will be decompresed");

		try {
			gzipInputStream = new GZIPInputStream(new FileInputStream(absolutePath));
			out = new FileOutputStream(new File(Path.LANGUAGE_TRAINEDDATA_DIR + this.uncompressFileName), false);

			byte[] buf = new byte[1024]; // size can be changed according to programmer's need.
			int len;
			while ((len = gzipInputStream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			gzipInputStream.close();
			out.close();
			new File(Path.LANGUAGE_TRAINEDDATA_DIR + this.downloadFileName).delete();
			serviceMessenger.sendMessage(NetworkHelper.DOWNLOAD_SUCCESSFULLY, 6, null);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Could not decompress File, file not find!", e);
			serviceMessenger.sendMessage(0, 6, new Exception(e.getMessage()));
		} catch (IOException e) {
			Log.e(TAG, "Could not decompress file!", e);
			serviceMessenger.sendMessage(0, 6, new Exception(e.getMessage()));
		}
	}

	@Override
	protected void onCancelled() {

	}

	private static final String TAG = DownloadOcrTraineddata.class.getSimpleName();
	private ProgressDialog mProgressDialog;

	/**
	 * @param progressDialog
	 *            Component to show Progress or null if the download should be not visible
	 */
	public void setmProgressDialog(ProgressDialog progressDialog, String downloadDialogtext) {
		this.mProgressDialog = progressDialog;
		this.mProgressDialog.setMessage(downloadDialogtext + downloadFileName);
	}

	private final String downloadFileName;
	private final String uncompressFileName;
	public static final int DIALOG_DOWNLOAD_PROGRESS = 10;

	private final Handler handler;
	private final ServiceMessenger serviceMessenger;
}
