package no.siriuslabs.image;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WSuggestionPopup;
import eu.webtoolkit.jwt.WValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget representing an annotation triplet of three fields (subject, predicate, object).
 */
public class TripletWidget extends WContainerWidget {

	private WLineEdit part1;
	private WSuggestionPopup part1Popup;

	private WLineEdit part2;
	private WSuggestionPopup part2Popup;

	private WLineEdit part3;
	private WSuggestionPopup part3Popup;

	public TripletWidget() {
		initialize();
		addSuggestions();
	}

	private void initialize() {
		WHBoxLayout layout = new WHBoxLayout();
		setLayout(layout);

		WSuggestionPopup.Options options = new WSuggestionPopup.Options();
		options.highlightBeginTag = "<span class=\"highlight\">";
		options.highlightEndTag = "</span>";
		options.listSeparator = ',';
		options.whitespace = " \\n";
		options.wordSeparators = "-., \"@\\n;";
		options.appendReplacedText = ", ";

		part1 = new WLineEdit();
//		part1.setEmptyText("Part 1");
		part1Popup = new WSuggestionPopup(options);
		part1Popup.forEdit(part1);
		part1.setValidator(new WValidator(true));
		layout.addWidget(part1);

		part2 = new WLineEdit();
		part2Popup = new WSuggestionPopup(options);
		part2Popup.forEdit(part2);
		part2.setValidator(new WValidator(true));
		layout.addWidget(part2);

		part3 = new WLineEdit();
		part3Popup = new WSuggestionPopup(options);
		part3Popup.forEdit(part3);
		part3.setValidator(new WValidator(true));
		layout.addWidget(part3);
	}

	private void addSuggestions() {
//		part1Popup.addSuggestion("Part 1a");
//		part1Popup.addSuggestion("Part 1b");
//		part1Popup.addSuggestion("Part 1c");
	}

	public List<WValidator.State> validate() {
		List<WValidator.State> results = new ArrayList<>(3);
		results.add(part1.validate());
		results.add(part2.validate());
		results.add(part3.validate());
		return results;
	}

	public String getPart1Value() {
		return part1.getValueText().trim();
	}

	public String getPart2Value() {
		return part2.getValueText().trim();
	}

	public String getPart3Value() {
		return part3.getValueText().trim();
	}

}
