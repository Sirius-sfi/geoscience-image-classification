package no.siriuslabs.image;

import eu.webtoolkit.jwt.WBorderLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMenu;
import eu.webtoolkit.jwt.WWidget;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.ui.container.AbstractAnnotationContainer;
import no.siriuslabs.image.ui.container.AnnotationContainer;
import no.siriuslabs.image.ui.container.FeedbackContainer;
import no.siriuslabs.image.ui.container.HomeContainer;
import no.siriuslabs.image.ui.container.ImageSelectionContainer;
import no.siriuslabs.image.ui.container.QueryContainer;
import no.siriuslabs.image.ui.container.UploadContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The main frontend application.
 */
public class FrontendApplication extends AbstractAnnotationApplication implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FrontendApplication.class);

	public static final String APPLICATION_TITLE = "Geoscience Image Classification";
	private static final String HOME_LABEL = "Home";
	private static final String UPLOAD_LABEL = "Upload Image";
	private static final String ANNOTATE_LABEL = "Annotate Image";
	private static final String QUERY_LABEL = "Query Data";
	private static final String FEEDBACK_LABEL = "Feedback";

	private WContainerWidget centerContainer;
	private WBorderLayout layout;
	private WMenu menu;
	private WContainerWidget northernSpacingContainer;

	public FrontendApplication(WEnvironment env) {
		super(env);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

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
		menu.addItem(QUERY_LABEL);
		menu.addItem(FEEDBACK_LABEL);

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

	@Override
	public String getApplicationTitle() {
		return APPLICATION_TITLE;
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
		else if(QUERY_LABEL.equalsIgnoreCase(currentSelection)) {
			displayContainer(createQueryContainer());
		}
		else if(FEEDBACK_LABEL.equalsIgnoreCase(currentSelection)) {
			displayContainer(createFeedbackContainer());
		}
	}

	private void displayContainer(WContainerWidget container) {
		WWidget toRemove = layout.widgetAt(WBorderLayout.Position.Center);
		if(toRemove != null) {
			LOGGER.info("Removing container of type {}", toRemove.getClass().getSimpleName());
			layout.removeWidget(toRemove);
			if(toRemove instanceof AbstractAnnotationContainer) {
				((AbstractAnnotationContainer)toRemove).cleanupOnLeave();
			}
		}

		LOGGER.info("Placing new center element of type {}", container.getClass().getSimpleName());
		layout.addWidget(container, WBorderLayout.Position.Center);
	}

	private HomeContainer createHomeContainer() {
		LOGGER.info("Creating new HomeContainer");
		return new HomeContainer(this, centerContainer);
	}

	private UploadContainer createUploadContainer() {
		LOGGER.info("Creating new UploadContainer");
		return new UploadContainer(this, centerContainer);
	}

	private ImageSelectionContainer createSelectContainer() {
		LOGGER.info("Creating new ImageSelectionContainer");

		final ImageSelectionContainer imageSelectionContainer = new ImageSelectionContainer(this, centerContainer);
		imageSelectionContainer.addPropertyChangeListener(this); // TODO check if we are leaking memory on removal because opf this

		return imageSelectionContainer;
	}

	private AnnotationContainer createAannotationContainer(GeologicalImage image) {
		LOGGER.info("Creating new AnnotationContainer for image {}", image.getLabel());
		return new AnnotationContainer(this, centerContainer, image);
	}

	private QueryContainer createQueryContainer() {
		LOGGER.info("Creating new QueryContainer");
		return new QueryContainer(this, centerContainer);
	}

	private FeedbackContainer createFeedbackContainer() {
		LOGGER.info("Creating new FeedbackContainer");
		return new FeedbackContainer(this, centerContainer);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(ImageSelectionContainer.START_ANNOTATING_PROPERTY_NAME.equals(evt.getPropertyName()) && evt.getNewValue() != null) {
			GeologicalImage image = (GeologicalImage) evt.getNewValue();
			LOGGER.info(ImageSelectionContainer.START_ANNOTATING_PROPERTY_NAME + " triggered - image selected is {}", image.getLabel());
			displayContainer(createAannotationContainer(image));
		}
	}
}
