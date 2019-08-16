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

		WText textIntro = new WText("<b>We appreciate your feedback regarding this tool and ontology. Please take a look at the most common cases below and drop us an email by clicking the button below. " +
				"Please note that we might not be able to reply to every email, but we will take all suggestions into account.</b>");
		layout.addWidget(textIntro, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		addSpacerWidget(layout);

		WText textBugs = new WText("<b>Bug reports:</b>" +
				"<p>If you encounter an error in the software like something just not doing what it should do or you being “thrown back” to the startup page, please let us know. " +
				"Send us a description of what you did, what went wrong and if it was reproducible. The more detailed, the better – it is hard to fix errors for situations you cannot understand or recreate. " +
				"If possible you can attach a screenshot, too.</p>");
		layout.addWidget(textBugs, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		WText textFeatures = new WText("<b>Feature suggestions:</b>" +
				"<p>If you think some feature or functionality is missing in the tool, please send us a description of what you would like to see and how this could improve the tool and make your daily work easier.</p>");
		layout.addWidget(textFeatures, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		WText textOntology = new WText("<b>Ontology requests:</b>" +
				"<p>If you find something missing in the vocabulary of the ontology (subjects, predicates, objects or relations) or think something should be renamed in a different way, please explain what you would like to have changed and why.</p>");
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
