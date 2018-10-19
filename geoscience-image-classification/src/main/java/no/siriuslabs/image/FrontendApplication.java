package no.siriuslabs.image;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;

public class FrontendApplication extends WApplication {

	public FrontendApplication(WEnvironment env) {
		super(env);

		setTitle("Geoscience Image Classification");


	}
}
