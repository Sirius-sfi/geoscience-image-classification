package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WText;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.FrontendApplication;

/**
 * Container class representing the query screen.
 */
public class QueryContainer extends AbstractAnnotationContainer {

	/**
	 * Constructor taking the application and parent container.
	 */
	public QueryContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);

		initializeContents();
	}

	private void initializeContents() {
		WHBoxLayout layout = new WHBoxLayout();

		addSpacerWidget(layout);

		WText text = new WText(FrontendApplication.APPLICATION_TITLE + " uses SemFacet to query data. ");
		layout.addWidget(text);

		WLink link = new WLink("/semFacet/");
		WAnchor anchor = new WAnchor(link, "Open SemFacet");
		anchor.setTarget(AnchorTarget.TargetNewWindow);
		layout.addWidget(anchor);

		addSpacerWidget(layout);

		setLayout(layout);
	}

	private void addSpacerWidget(WHBoxLayout layout) {
		layout.addWidget(new WText(""), 1);
	}

}
