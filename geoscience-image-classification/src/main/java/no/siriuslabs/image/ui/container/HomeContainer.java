package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;
import no.siriuslabs.image.AbstractAnnotationApplication;

public class HomeContainer extends AbstractAnnotationContainer {

	public HomeContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);

		addWidget(new WText("Home"));
	}
}
