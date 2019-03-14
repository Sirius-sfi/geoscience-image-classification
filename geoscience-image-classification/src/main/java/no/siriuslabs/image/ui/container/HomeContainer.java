package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WText;
import no.siriuslabs.image.AbstractAnnotationApplication;

import java.util.EnumSet;

public class HomeContainer extends AbstractAnnotationContainer {

	public HomeContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);

		WHBoxLayout layout = new WHBoxLayout();
		layout.addWidget(new WText("<h1>SIRIUS GeoAnnotator</h1>"), 1, EnumSet.of(AlignmentFlag.AlignCenter));
		setLayout(layout);
	}
}
