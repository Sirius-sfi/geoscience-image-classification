package no.siriuslabs.image;

import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMenu;
import eu.webtoolkit.jwt.WStackedWidget;
import eu.webtoolkit.jwt.WTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/**
 * The main frontend application.
 */
public class FrontendApplication extends WApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(FrontendApplication.class);

	private final ServletContext context;

	public FrontendApplication(WEnvironment env) {
		super(env);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		setTitle("Geoscience Image Classification");

		context = getEnvironment().getServer().getServletContext();

		WContainerWidget container = new WContainerWidget();
		WStackedWidget contents = new WStackedWidget();
		WMenu menu = new WMenu(contents, Orientation.Vertical, container);
		menu.setStyleClass("nav nav-pills nav-stacked");
		menu.setWidth(new WLength(150));
		menu.addItem("Home", new HomeContainer(container));
		menu.addItem("Upload Image", new UploadContainer(this, container));
		menu.addItem("Annotate Image", new WTextArea("Annotate Image"));
		container.addWidget(contents);


		WContainerWidget root = getRoot();
		root.addWidget(container);

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	protected ServletContext getServletContext() {
		return context;
	}
}
