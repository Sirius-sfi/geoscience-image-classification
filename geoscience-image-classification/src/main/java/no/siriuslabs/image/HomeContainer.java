package no.siriuslabs.image;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;

public class HomeContainer extends WContainerWidget {

	public HomeContainer(WContainerWidget parent) {
		super(parent);

		addWidget(new WText("Home"));
	}
}
