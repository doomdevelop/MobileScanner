package de.beutch.bachelorwork.util.file;

import android.os.Environment;

public class Path {
	// Test pictures :

	// Directory's :
	public static final String MAIN_APP_DIR = "";
	public static final String EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String MAIN_DIR = EXTERNAL_STORAGE_DIR + "/PixelToSpeech";

	// public static final String MAIN_LANGUAGE_PATH = Environment.getExternalStorageDirectory() + "/tesseract/";
	// public static final String MAIN_PICTURES_PATH = Environment.getExternalStorageDirectory() + "/pictures/";
	public static final String MAIN_PICTURES_PATH = EXTERNAL_STORAGE_DIR + "/PixelToSpeech/pictures/";
	// public static final String MAIN_DIR = EXTERNAL_STORAGE_DIR + "/PixelToSpeech";
	public static final String MAIN_LANGUAGE_PATH = EXTERNAL_STORAGE_DIR + "/PixelToSpeech/tesseract/";
	public static final String GRAYSCALE = MAIN_PICTURES_PATH + "grayscale/";
	public static final String CONTRAST = MAIN_PICTURES_PATH + "contrast/";
	public static final String LANGUAGE_TRAINEDDATA_DIR = MAIN_LANGUAGE_PATH + "tessdata/";

	/*
	 * // Directory's :
	 * 
	 * public static final String EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
	 * public static final String MAIN_DIR = EXTERNAL_STORAGE_DIR + "/Pixel-To-Speech";
	 * public static final String MAIN_LANGUAGE_PATH = MAIN_DIR + "/tesseract/";
	 * public static final String MAIN_PICTURES_PATH = MAIN_DIR + "/pictures/";
	 * public static final String GRAYSCALE = MAIN_PICTURES_PATH + "/grayscale/";
	 * public static final String CONTRAST = MAIN_PICTURES_PATH + "/contrast/";
	 */
	/**
	 * This is Dir for language traineddata witch is tesseract using.
	 * DO not call it to init tesseract, use MAIN_LANGUAGE_PATH for that
	 */

}
