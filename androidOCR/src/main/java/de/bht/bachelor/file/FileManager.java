package de.bht.bachelor.file;

import java.io.File;

import android.util.Log;
import de.beutch.bachelorwork.util.file.Path;

public class FileManager {
	/**
	 * Check if on the local SdCard are all indispensable files and directories.
	 * If not , missing directory will be created.
	 */
	public void prepareFileSystem() {
		Log.d(TAG, "prepareFileSystem()..."+Path.EXTERNAL_STORAGE_DIR);
		File file = new File(Path.EXTERNAL_STORAGE_DIR);
		if (!file.exists()) {
			new Exception("No External storage !");
		}
		boolean mainDir = check(new File(Path.MAIN_DIR), true);
		boolean  mainPicDir = check(new File(Path.MAIN_PICTURES_PATH), true);
		boolean  mainLanguageDir = check(new File(Path.MAIN_LANGUAGE_PATH), true);
		boolean mainTraineddataDir = check(new File(Path.LANGUAGE_TRAINEDDATA_DIR), true);
	Log.d(TAG, "mainDir: "+mainDir+", mainPicDir: "+mainPicDir+", mainLanguageDir: "+mainLanguageDir+", mainTraineddataDir: "+mainTraineddataDir);
	}

	/**
	 * Delete all files from {@link Path.MAIN_PICTURES_PATH} with names not containing given prefix
	 * 
	 * @param prefix
	 */
	public void cleanSdCard(String prefix) {
		File[] files;
		String fileName;
		File root = new File(Path.MAIN_PICTURES_PATH);
		files = root.listFiles();
		Log.d(TAG, "Found " + files.length + files);
		for (File file : files) {
			if (!file.isDirectory()) {
				fileName = file.getName();
				Log.d(TAG, "Check file : " + fileName);
				if (!fileName.contains(prefix)) {
					file.delete();
				}
			}
		}
	}

	/**
	 * check for given file
	 * 
	 * @param file
	 *            file to check if exist
	 * @param create
	 *            if true and if file is not existing , file will be create
	 */
	public boolean check(File file, boolean create) {
		boolean exist = true;
		if (!file.exists()) {
			exist = false;
			if (create && !file.mkdir()) {
				Log.d(TAG, "Could not create file : " + file.getAbsoluteFile());
			}
		}
		if (!file.canWrite()) {
			Log.d(TAG, "Could not write in to the file : " + file.getAbsoluteFile());
		}
		return exist;
	}

	private static final String TAG = FileManager.class.getSimpleName();
}
