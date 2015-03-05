package de.beutch.bachelorwork.util.language;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageUtil {

	public static List<String> convertLanguagesToIso3(List<String> isoLanguages) {
		for (String s : isoLanguages) {
			iso3Languages.add(new Locale(s).getISO3Language());
		}
		return iso3Languages;
	}

	public static List<String> convarteLanguagesToDisplayed(List<String> isoLanguages) {
		for (String s : isoLanguages) {
			displayedLanguages.add(new Locale(s).getDisplayLanguage());
		}
		return displayedLanguages;
	}

	/**
	 * 
	 * @param l
	 *            Locale as language
	 * @return true if the Language is compatible with ISO3
	 */
	public boolean isLanguageAccordantWithISO3(String l) {
		return iso3Languages.contains(l);
	}

	public List<Number> getWrongNotAccordianLanguages(List<String> languages) {

		return new ArrayList<Number>();
	}

	public List<String> getIso3Languages() {
		return iso3Languages;
	}

	private final static List<String> iso3Languages = new ArrayList<String>();

	private final static List<String> displayedLanguages = new ArrayList<String>();

	public List<String> getDisplayedLanguages() {
		return displayedLanguages;
	}
}
