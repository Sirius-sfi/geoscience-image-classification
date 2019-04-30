package no.siriuslabs.image.ui;

import eu.webtoolkit.jwt.WSuggestionPopup;

/**
 * Helper class to support using the auto-complete functionality in widgets and containers.
 */
public final class AutocompleteHelper {

	private AutocompleteHelper() {
	}

	/**
	 * Creates a standardized set of options to configure a WSuggestionPopup;
	 */
	public static WSuggestionPopup.Options createOptions() {
		WSuggestionPopup.Options options = new WSuggestionPopup.Options();
		options.highlightBeginTag = "<span class=\"highlight\">";
		options.highlightEndTag = "</span>";
		options.listSeparator = ',';
		options.whitespace = " \\n";
		options.wordSeparators = "-., \"@\\n;";
		options.appendReplacedText = ", ";

		return options;
	}

	/**
	 * Removes a possible trailing comma from the contents of a field working with a WSuggestionPopup.
	 */
	public static String removeAutoCompleteComma(String text) {
		text = text.replace(',', ' ');
		text = text.trim();
		return text;
	}

}
