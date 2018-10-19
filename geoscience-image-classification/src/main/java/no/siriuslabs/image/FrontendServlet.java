package no.siriuslabs.image;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class FrontendServlet extends WtServlet {

	private static final long serialVersionUID = 1578912809839556880L;

	@Override
	public WApplication createApplication(WEnvironment wEnvironment) {
		return new FrontendApplication(wEnvironment);
	}
}
