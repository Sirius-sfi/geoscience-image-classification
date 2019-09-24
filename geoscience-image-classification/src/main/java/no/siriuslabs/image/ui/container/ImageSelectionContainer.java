package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGroupBox;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWidget;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.Image;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Container class representing the image selection page.
 */
public class ImageSelectionContainer extends AbstractAnnotationContainer implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageSelectionContainer.class);

	public static final String START_ANNOTATING_PROPERTY_NAME = "imageSelectionContainer.startAnnotating";

	private static final String GEOLOGICAL_IMAGE_TYPE_TEXT = "Geological image";
	private static final int NUMBER_OF_RECENT_IMAGES = 5;

	private static final String RECENTLY_UPLOADED_LABEL = "Recently Uploaded";
	private static final String ALL_IMAGES_LABEL = "All Images";

	private final PropertyChangeSupport propertyChangeSupport;

	private WTabWidget tabWidget;
	private WContainerWidget filterPanel;
	private WComboBox typeFilterCombobox;
	private WContainerWidget recentImagesTab;
	private WVBoxLayout recentImagesLayout;
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
		recentImagesLayout = new WVBoxLayout();
		recentImagesTab = new WContainerWidget();
		recentImagesTab.setLayout(recentImagesLayout);

		loadAndShowRecentImages();
	}

	private void loadAndShowRecentImages() {
		removePreviewWidgets(recentImagesTab, recentImagesLayout);

		List<GeologicalImage> images = getImageAnnotationAPI().getImagesOfGivenType(getSessionID(), GEOLOGICAL_IMAGE_TYPE_TEXT);

		List<GeologicalImage> imageCandidates = new ArrayList<>(images.size());
		for(GeologicalImage image : images) {
			if(hasValidSubmissionDate(image)) {
				imageCandidates.add(image);
			}
		}

		imageCandidates.sort((GeologicalImage o1, GeologicalImage o2) -> {
			String dateO1 = o1.getDateSubmission();
			String dateO2 = o2.getDateSubmission();
			// we sort descending here, hence the * -1
			return dateO1.compareTo(dateO2) * -1;
		});

		LOGGER.info("loading recent images - picking up to " + NUMBER_OF_RECENT_IMAGES + " out of {} candidates of {} images", imageCandidates.size(), images.size());

		List<GeologicalImage> recentImages = new ArrayList<>(NUMBER_OF_RECENT_IMAGES);
		if(imageCandidates.size() > NUMBER_OF_RECENT_IMAGES) {
			recentImages = IntStream.range(0, NUMBER_OF_RECENT_IMAGES).mapToObj(imageCandidates::get).collect(Collectors.toCollection(() -> new ArrayList<>(NUMBER_OF_RECENT_IMAGES)));
		}
		else {
			recentImages.addAll(imageCandidates);
		}

		initializePreviewImageWidgets(recentImages, recentImagesLayout);
	}

	private boolean hasValidSubmissionDate(GeologicalImage image) {
		return image.getDateSubmission() != null && !image.getDateSubmission().trim().isEmpty() && !Image.DEFAULT_SUBMISSION.equals(image.getDateSubmission());
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

		tabWidget.addTab(recentImagesTab, RECENTLY_UPLOADED_LABEL, WTabWidget.LoadPolicy.LazyLoading);
		tabWidget.addTab(allImagesTab, ALL_IMAGES_LABEL, WTabWidget.LoadPolicy.LazyLoading);
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
		removePreviewWidgets(allImagesTab, allImagesLayout);

		// load images of selected type
		String sessionID = getSessionID();
		ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		String selectedType = typeFilterCombobox.getValueText();
		LOGGER.info("loading all images of type {}", selectedType);
		List<GeologicalImage> images = imageAnnotationAPI.getImagesOfGivenType(sessionID, selectedType);

		// sort by image name while keeping the grouping by image type
		images.sort((GeologicalImage o1, GeologicalImage o2) -> {
			if(o1.getTypeLabel().equals(o2.getTypeLabel())) {
				String title1 = o1.getVisualRepresentation();
				String title2 = o2.getVisualRepresentation();
				return title1.compareToIgnoreCase(title2);
			}

			return 0;
		});

		initializePreviewImageWidgets(images, allImagesLayout);
	}

	private void removePreviewWidgets(WContainerWidget tab, WLayout layout) {
		LOGGER.info("removing existing previews");
		// find and remove displayed preview images
		List<WWidget> groupBoxesToRemove = new ArrayList<>();
		for(WWidget widget : tab.getChildren()) {
			if(widget instanceof WGroupBox) {
				groupBoxesToRemove.add(widget);
			}
		}

		for(WWidget widget : groupBoxesToRemove) {
			layout.removeWidget(widget);
		}
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
		else if(PreviewSelectionWidget.IMAGE_DELETED_PROPERTY_NAME.equals(evt.getPropertyName())) {
			if(tabWidget.getCurrentItem().getText().toString().equals(RECENTLY_UPLOADED_LABEL)) {
				LOGGER.info(RECENTLY_UPLOADED_LABEL + " active - reloading");
				loadAndShowRecentImages();
			}
			else {
				LOGGER.info(ALL_IMAGES_LABEL + " active - reloading");
				searchButtonClickedAction();
			}
		}
	}
}
