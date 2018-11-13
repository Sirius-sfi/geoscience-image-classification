package no.siriuslabs.image;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBorderLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMenu;
import eu.webtoolkit.jwt.WWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/**
 * The main frontend application.
 */
public class FrontendApplication extends WApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(FrontendApplication.class);

	private static final String APPLICATION_TITLE = "Geoscience Image Classification";
	private static final String HOME_LABEL = "Home";
	private static final String UPLOAD_LABEL = "Upload Image";
	private static final String ANNOTATE_LABEL = "Annotate Image";

	private final ServletContext context;

	private WContainerWidget centerContainer;
	private WBorderLayout layout;
	private WMenu menu;
	private WContainerWidget northernSpacingContainer;

	public FrontendApplication(WEnvironment env) {
		super(env);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		context = getEnvironment().getServer().getServletContext();

		setTitle(APPLICATION_TITLE);

		initializeCenterContainer();
		initializeMenu();
		initializeSpacingContainer();

		initializeLayout();

		WContainerWidget root = getRoot();
		root.addWidget(centerContainer);

		initializeWithDefaultValues();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializeCenterContainer() {
		centerContainer = new WContainerWidget();
	}

	private void initializeMenu() {
		menu = new WMenu(centerContainer);
		menu.setStyleClass("nav nav-pills nav-stacked");
		menu.setWidth(new WLength(180));

		menu.addItem(HOME_LABEL);
		menu.addItem(UPLOAD_LABEL);
		menu.addItem(ANNOTATE_LABEL);

		menu.itemSelected().addListener(this, () -> performMenuSelectionChangedAction(menu));
	}

	private void initializeSpacingContainer() {
		northernSpacingContainer = new WContainerWidget();
		northernSpacingContainer.setMinimumSize(WLength.Auto, new WLength(50, WLength.Unit.Pixel));
	}

	private void initializeLayout() {
		layout = new WBorderLayout();
		layout.addWidget(menu, WBorderLayout.Position.West);
		layout.addWidget(northernSpacingContainer, WBorderLayout.Position.North);

		centerContainer.setLayout(layout);
	}

	private void initializeWithDefaultValues() {
		menu.select(0);
	}

	private void performMenuSelectionChangedAction(WMenu menu) {
		final String currentSelection = menu.getCurrentItem().getText().getValue();
		if(HOME_LABEL.equals(currentSelection)) {
			displayContainer(createHomeContainer());
		}
		else if(UPLOAD_LABEL.equals(currentSelection)) {
			displayContainer(createUploadContainer());
		}
		else if(ANNOTATE_LABEL.equals(currentSelection)) {
			displayContainer(createSelectContainer());
		}
	}

	private void displayContainer(WContainerWidget container) {
		WWidget toRemove = layout.widgetAt(WBorderLayout.Position.Center);
		if(toRemove != null) {
			LOGGER.info("Removing container of type {}", toRemove.getClass().getSimpleName());
			layout.removeWidget(toRemove);
		}

		LOGGER.info("Placing new center element of type {}", container.getClass().getSimpleName());
		layout.addWidget(container, WBorderLayout.Position.Center);
	}

	private HomeContainer createHomeContainer() {
		LOGGER.info("Creating new HomeContainer");
		return new HomeContainer(centerContainer);
	}

	private UploadContainer createUploadContainer() {
		LOGGER.info("Creating new UploadContainer");
		return new UploadContainer(this, centerContainer);
	}

	private ImageSelectionContainer createSelectContainer() {
		LOGGER.info("Creating new ImageSelectionContainer");
		return new ImageSelectionContainer(this, centerContainer);
	}

	private AnnotationContainer createAannotationContainer(String imagePath, String imageAbsoluteFilePath) {
		LOGGER.info("Creating new AnnotationContainer for image {}", imagePath);
		return new AnnotationContainer(this, centerContainer, imagePath, imageAbsoluteFilePath);
	}

	protected ServletContext getServletContext() {
		return context;
	}

	protected void showAnnotationContainer(String imagePath, String imageAbsoluteFilePath) {
		displayContainer(createAannotationContainer(imagePath, imageAbsoluteFilePath));
	}
}
