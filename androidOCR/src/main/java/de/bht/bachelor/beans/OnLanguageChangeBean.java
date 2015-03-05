package de.bht.bachelor.beans;

import java.util.Locale;

public class OnLanguageChangeBean {
	public OnLanguageChangeBean(int backUpIso3LanguagePosition, String backUpIso3Language, String newIso3Language, Locale newLocale, int newPosition) {
		this.backUpIso3LanguagePosition = backUpIso3LanguagePosition;
		this.backUpIso3Language = backUpIso3Language;
		this.newIso3Language = newIso3Language;
		this.newLocale = newLocale;
		this.newPosition = newPosition;
	}

	public int getBackUpIso3LanguagePosition() {
		return backUpIso3LanguagePosition;
	}

	public String getBackUpIso3Language() {
		return backUpIso3Language;
	}

	public String getNewIso3Language() {
		return newIso3Language;
	}

	public int getStatusAfterDownload() {
		return statusAfterDownload;
	}

	public void setStatusAfterDownload(int statusAfterDownload) {
		this.statusAfterDownload = statusAfterDownload;
	}

	public int getNewPosition() {
		return newPosition;
	}

	public Locale getNewLocale() {
		return newLocale;
	}

	private int backUpIso3LanguagePosition = -1;
	private final String backUpIso3Language;
	private final String newIso3Language;
	private int statusAfterDownload = -1;
	private final int newPosition;
	private final Locale newLocale;
}
