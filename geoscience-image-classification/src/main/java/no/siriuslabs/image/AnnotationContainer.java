package no.siriuslabs.image;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.model.GeologicalImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationContainer extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationContainer.class);

	private final FrontendApplication application;
	private final GeologicalImage image;

	public AnnotationContainer(FrontendApplication application, WContainerWidget parent, GeologicalImage image) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;
		this.image = image;

		WVBoxLayout layout = new WVBoxLayout();
		layout.addWidget(new WText(image.getLabel()));
		layout.addWidget(new WText(image.getTypeLabel()));

		setLayout(layout);

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

}
