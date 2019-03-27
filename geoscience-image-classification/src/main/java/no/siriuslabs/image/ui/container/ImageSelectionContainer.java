package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGroupBox;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWidget;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.services.ImageFileService;
import no.siriuslabs.image.ui.widget.PreviewSelectionWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Container class representing the image selection page.
 */
public class ImageSelectionContainer extends AbstractAnnotationContainer implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageSelectionContainer.class);

	public static final String START_ANNOTATING_PROPERTY_NAME = "imageSelectionContainer.startAnnotating";
	private static final String GEOLOGICAL_IMAGE_TYPE_TEXT = "Geological image";

	private final PropertyChangeSupport propertyChangeSupport;

	private WTabWidget tabWidget;
	private WContainerWidget filterPanel;
	private WComboBox typeFilterCombobox;
	private WContainerWidget recentImagesTab;
	private WContainerWidget allImagesTab;
	private WVBoxLayout allImagesLayout;

	private List<Concept> imageTypes;

	/**
	 * Constructor taking the application and the parent container.
	 */
	public ImageSelectionContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		propertyChangeSupport = new PropertyChangeSupport(this);

		initializeRecentImagesTab();
		initializeAllImagesTab();

		initializeTabWidget();

		initializeMainLayout();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializeRecentImagesTab() {
		WVBoxLayout recentImagesLayout = new WVBoxLayout();
		recentImagesTab = new WContainerWidget();
		recentImagesTab.setLayout(recentImagesLayout);

//		String sessionID = getSessionID();
//		ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();
//
//		// TODO replace with selection of recent images
//		List<GeologicalImage> images = imageAnnotationAPI.getImagesOfGivenType(sessionID, GEOLOGICAL_IMAGE_TYPE_TEXT);
//
//		initializePreviewImageWidgets(images, recentImagesLayout);
	}

	private void initializeAllImagesTab() {
		String sessionID = getSessionID();
		ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		allImagesTab = new WContainerWidget();
		allImagesLayout = new WVBoxLayout();
		allImagesTab.setLayout(allImagesLayout);

		imageTypes = imageAnnotationAPI.getImageTypes(sessionID);
		initializeFilterPanel();
		allImagesLayout.addWidget(filterPanel);
	}

	private void initializeTabWidget() {
		tabWidget = new WTabWidget();
		tabWidget.setOverflow(Overflow.OverflowAuto);

		tabWidget.addTab(recentImagesTab, "Recently Uploaded", WTabWidget.LoadPolicy.LazyLoading);
		tabWidget.addTab(allImagesTab, "All Images", WTabWidget.LoadPolicy.LazyLoading);
	}

	private void initializeMainLayout() {
		WVBoxLayout layout = new WVBoxLayout();
		layout.addWidget(tabWidget);
		setLayout(layout);
	}

	private void initializeFilterPanel() {
		filterPanel = new WContainerWidget();
		filterPanel.setMinimumSize(WLength.Auto, new WLength(50));

		WHBoxLayout filterLayout = new WHBoxLayout();

		WLabel filterLabel = new WLabel("Image Type:");
		filterLayout.addWidget(filterLabel, 0, EnumSet.of(AlignmentFlag.AlignBottom));

		typeFilterCombobox = new WComboBox();
		typeFilterCombobox.setMaximumSize(new WLength(300), WLength.Auto);
		filterLayout.addWidget(typeFilterCombobox, 0, EnumSet.of(AlignmentFlag.AlignBottom));
		filterLabel.setBuddy(typeFilterCombobox);

		for(Concept concept : imageTypes) {
			typeFilterCombobox.addItem(concept.getVisualRepresentation());
		}
		if(!imageTypes.isEmpty()) {
			typeFilterCombobox.setCurrentIndex(0);
		}

		WPushButton searchButton = new WPushButton("Search");
		searchButton.clicked().addListener(this, (WMouseEvent arg) -> searchButtonClickedAction());
		filterLayout.addWidget(searchButton, 0, EnumSet.of(AlignmentFlag.AlignBottom));

		filterLayout.addWidget(new WLabel(" "), 1); // spacer label

		filterPanel.setLayout(filterLayout);
	}

	private void initializePreviewImageWidgets(List<GeologicalImage> images, WVBoxLayout layout) {
		ImageFileService fileService = (ImageFileService) getFileService();

		WGroupBox groupBox = new WGroupBox();
		layout.addWidget(groupBox);

		for(GeologicalImage image : images) {
			LOGGER.info("setting up image: {}", image.getName());

			String path = fileService.getRelativeImagePathForFile(image.getLocation());
			image.setRelativeImagePath(path);
			String absolutePath = fileService.getAbsolutePathForFile(image.getLocation());
			image.setAbsoluteImagePath(absolutePath);

			PreviewSelectionWidget previewWidget = new PreviewSelectionWidget(this, image);
			previewWidget.addPropertyChangeListener(this);
			previewWidget.setMargin(new WLength(50), EnumSet.of(Side.Bottom));

			if(!groupBox.getTitle().getValue().equals(image.getTypeLabel())) {
				groupBox = new WGroupBox();
				groupBox.setMargin(new WLength(50), EnumSet.of(Side.Bottom));
				layout.addWidget(groupBox);
			}

			groupBox.setTitle(image.getTypeLabel());
			groupBox.addWidget(previewWidget);
		}
	}

	private void searchButtonClickedAction() {
		// find and remove displayed preview images
		List<WWidget> groupBoxesToRemove = new ArrayList<>();
		for(WWidget widget : allImagesTab.getChildren()) {
			if(widget instanceof WGroupBox) {
				groupBoxesToRemove.add(widget);
			}
		}

		for(WWidget widget : groupBoxesToRemove) {
			allImagesLayout.removeWidget(widget);
		}


		// load images of selected type
		String sessionID = getSessionID();
		ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		String selectedType = typeFilterCombobox.getValueText();
		List<GeologicalImage> images = imageAnnotationAPI.getImagesOfGivenType(sessionID, selectedType);

		initializePreviewImageWidgets(images, allImagesLayout);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(PreviewSelectionWidget.IMAGE_SELECTED_PROPERTY_NAME.equals(evt.getPropertyName()) && evt.getNewValue() != null) {
			LOGGER.info(PreviewSelectionWidget.IMAGE_SELECTED_PROPERTY_NAME + " triggered");
			propertyChangeSupport.firePropertyChange(START_ANNOTATING_PROPERTY_NAME, null, evt.getNewValue());
		}
	}
}
