package no.siriuslabs.image;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationContainer extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationContainer.class);

	private final FrontendApplication application;
	private final String imagePath;
	private final String imageAbsoluteFilePath;

	public AnnotationContainer(FrontendApplication application, WContainerWidget parent, String imagePath, String imageAbsoluteFilePath) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;
		this.imagePath = imagePath;
		this.imageAbsoluteFilePath = imageAbsoluteFilePath;

		addWidget(new WText(imagePath));

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

}
