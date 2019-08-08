package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.AbstractAnnotationApplication;

import java.util.EnumSet;

/**
 * Container class representing the feedback screen.
 */
public class FeedbackContainer extends AbstractAnnotationContainer {

	private static final String FEEDBACK_MAIL_ADDRESS = "irina.pene@geo.uio.no";
	private static final String MAIL_SUBJECT = "Image Annotator feedback";

	/**
	 * Constructor taking the main application and the parent container (to be passed to the super class).
	 */
	public FeedbackContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);

		initializeContents();
	}

	private void initializeContents() {
		WHBoxLayout outerLayout = new WHBoxLayout();
		addSpacerLayout(outerLayout);

		WVBoxLayout layout = initializeFeedbackSection();

		outerLayout.addLayout(layout);

		addSpacerLayout(outerLayout);

		setLayout(outerLayout);
	}

	private WVBoxLayout initializeFeedbackSection() {
		WVBoxLayout layout = new WVBoxLayout();
		addSpacerWidgetStretched(layout);

		WText textIntro = new WText("Introduction text here     Introduction text here     Introduction text here");
		layout.addWidget(textIntro, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		addSpacerWidget(layout);

		WText textBugs = new WText("<b>Bug reports:</b>" +
				"<p><i>Descriptive text here     Descriptive text here     Descriptive text here     Descriptive text here     Descriptive text here</i></p>");
		layout.addWidget(textBugs, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		WText textFeatures = new WText("<b>Feature suggestions:</b>" +
				"<p><i>Descriptive text here     Descriptive text here     Descriptive text here     Descriptive text here     Descriptive text here</i></p>");
		layout.addWidget(textFeatures, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		WText textOntology = new WText("<b>Ontology requests:</b>" +
				"<p><i>Descriptive text here     Descriptive text here     Descriptive text here     Descriptive text here     Descriptive text here</i></p>");
		layout.addWidget(textOntology, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		WHBoxLayout buttonLayout = initializeButtonLine();
		layout.addLayout(buttonLayout);

		addSpacerWidgetStretched(layout);
		return layout;
	}

	private WHBoxLayout initializeButtonLine() {
		WHBoxLayout buttonLayout = new WHBoxLayout();
		addSpacerWidgetStretched(buttonLayout);

		WPushButton sendMailButton = new WPushButton("Send Feedback Mail");
		WLink link = new WLink("mailto:" + FEEDBACK_MAIL_ADDRESS + "?subject=" + MAIL_SUBJECT);
		sendMailButton.setLink(link);
		buttonLayout.addWidget(sendMailButton);

		addSpacerWidgetStretched(buttonLayout);
		return buttonLayout;
	}

	private void addSpacerLayout(WHBoxLayout globalLayout) {
		WHBoxLayout spacerLayout = new WHBoxLayout();
		final WText text = new WText("");
		text.setMinimumSize(new WLength(100), WLength.Auto);
		spacerLayout.addWidget(text);
		globalLayout.addLayout(spacerLayout);
	}

	private void addSpacerWidget(WBoxLayout layout) {
		layout.addWidget(new WText(""), 1);
	}

	private void addSpacerWidgetStretched(WBoxLayout layout) {
		layout.addWidget(new WText(""), 1);
	}

}
