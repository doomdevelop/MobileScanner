package de.beutch.bachelorwork.util.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class FileUtil {

	/**
	 * 
	 * @param file
	 *            file to save
	 * @param format
	 *            for jpg see : Bitmap.CompressFormat.JPEG
	 */
	public boolean saveBitmapAsFile(Bitmap bitmap, File file, CompressFormat compressFormat) {

		FileOutputStream fileOutputStream;
		BufferedOutputStream bos;

		try {
			fileOutputStream = new FileOutputStream(file);
			bos = new BufferedOutputStream(fileOutputStream);
			bitmap.compress(compressFormat, 90, bos);
			try {
				bos.flush();
				bos.close();
				return true;
			} catch (IOException e) {
				Log.e(TAG, "Could not save a file in path : " + file.getAbsolutePath(), e);
			}
		} catch (NullPointerException e) {
			Log.e(TAG, "Could not save a file in path : " + file.getAbsolutePath(), e);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Could not save a file in path : " + file.getAbsolutePath(), e);
		}
		return false;
	}

	public File copyfile(String srFile, String dtFile) {
		if (!new File(dtFile).exists()) {
			new File(dtFile).mkdir();
		}
		File f2 = null;
		String[] splitFileName = splitFileNameForFromat(dtFile);
		String newfileName;
		Log.d(TAG, "src file : " + srFile);
		Log.d(TAG, "dest file : " + dtFile);
		try {
			File f1 = new File(srFile);
			if (splitFileName != null && splitFileName.length == 2) {
				newfileName = splitFileName[0] + "_" + getDateFormat() + "." + splitFileName[1];
				Log.d(TAG, "new File Name : " + newfileName);
				f2 = new File(newfileName);
			} else {
				f2 = new File(dtFile);
			}

			InputStream in = new FileInputStream(f1);

			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			Log.d(TAG, "File copied.");

		} catch (FileNotFoundException ex) {
			Log.e(TAG, "Could not copy file !", ex);
		} catch (IOException e) {
			Log.e(TAG, "Could not copy file !", e);
		}
		return f2;
	}

	public String[] splitFileNameForFromat(final String fileName) {
		String[] split = fileName.split("\\.");
		/*
		 * /mnt/cdcard/picture/fileName.jpg
		 * RESULT length: 2,
		 * path : /mnt/cdcard/picture/fileName
		 * fileName: jpg
		 */
		if (split.length == 2) {
			return split;
		}
		return getFileFormat(fileName);
	}

	/**
	 * Save data as it is. there is no configuration , no setting.
	 * The process run in separate thread.
	 * 
	 * @param data
	 * @param filePath
	 */
	public void saveDataAsJPG(final byte[] data, final String filePath) {
		Log.d(TAG, "saveDataAsJPG()....." + filePath);
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				FileOutputStream outStream;

				try {
					outStream = new FileOutputStream(filePath);
					outStream.write(data);
					outStream.close();

				} catch (NullPointerException e) {
					Log.e(TAG, "Could not save data as jpg", e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "Could not save data as jpg", e);
				}
			}
		};
		new Thread(runnable).start();
	}

	private String[] getFileFormat(final String fileName) {
		String result[] = new String[2];
		StringBuilder stringBuilder = new StringBuilder("");
		String tempFileName = fileName;

		if (!fileName.contains("."))
			return null;
		for (int i = fileName.length(); i <= 0; i--) {
			if (fileName.charAt(i) != '.') {
				stringBuilder.append(fileName.charAt(i));
			} else {
				result[0] = tempFileName.substring(i + 1, fileName.length() - 1);// save with no point
				result[1] = stringBuilder.toString();
				return result;
			}

		}
		return null;
	}


    /*
     * Convert YUV data int to RGB and save as Bitmap
     */
    public static void saveOrginalData(byte[] data,int w,int h) {
        Log.d(TAG, "saveOrginalData().......");

        try {
            int[] rgb = new int[w * h];
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

            ConvertUtil.decodeYUV420SP(rgb, data, w, h);
            bitmap.setPixels(rgb, 0, w, 0, 0, w, h);
            new FileUtil().saveBitmapAsFile(bitmap, new File(Path.MAIN_PICTURES_PATH + "orgBitmap.jpeg"), Bitmap.CompressFormat.JPEG);
        } catch (NullPointerException ex) {
            Log.d(TAG, "Could not ", ex);
        }
    }

	private String getDateFormat() {

		Format formater = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return formater.format(new Date());
	}

	private static final String TAG = FileUtil.class.getSimpleName();
}
