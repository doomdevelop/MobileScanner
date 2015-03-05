package de.bht.bachelor.exception;

public class NotInitializedTtsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotInitializedTtsException() {
		super();
	}

	public NotInitializedTtsException(String message) {
		super(message);
	}

	public NotInitializedTtsException(String message, String textToSpeech) {
		super(message);
		this.textToSpeech = textToSpeech;
	}

	private String textToSpeech;

	public String getTextToSpeech() {
		return textToSpeech;
	}
}
