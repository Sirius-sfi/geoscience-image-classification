package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.services.ImageFileService;
import no.siriuslabs.image.ui.container.ImageSelectionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;

/**
 * Widget combining an image preview, a link to the full size image and a selection button.
 */
public class PreviewSelectionWidget extends AbstractAnnotationWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewSelectionWidget.class);

	public static final String IMAGE_SELECTED_PROPERTY_NAME = "previewSelectionWidget.imageSelected";
	public static final String IMAGE_DELETED_PROPERTY_NAME = "previewSelectionWidget.imageDeleted";

	private static final int LINK_HEIGHT = 50;

	private final GeologicalImage image;

	private final PropertyChangeSupport propertyChangeSupport;

	private ImagePreviewWidget imagePreviewWidget;
	private WText imageInformation;
	private WAnchor anchor;

	private WContainerWidget buttonContainer;
	private WPushButton annotateButton;

	/**
	 * Constructor taking the parent container and the image to work with.
	 */
	public PreviewSelectionWidget(ImageSelectionContainer parent, GeologicalImage image) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.image = image;

		propertyChangeSupport = new PropertyChangeSupport(this);

		initializePreviewWidget();

		initializeImageInfo(image);
		initializeLink(image.getRelativeImagePath());
		initializeButtonPanel();

		initializeLayout();
		initializeDimensions();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializePreviewWidget() {
		imagePreviewWidget = new ImagePreviewWidget(image.getRelativeImagePath(), image.getAbsoluteImagePath());
	}

	private void initializeImageInfo(GeologicalImage image) {
		imageInformation = new WText();
		imageInformation.setText("Owned by <i><b>" + image.getContributor() + "</b></i>. Uploaded " + image.getDateSubmission());
	}

	private void initializeLink(String path) {
		WLink link = new WLink(path);
		anchor = new WAnchor(link, image.getLabel() + " (view full size)");
		anchor.setTarget(AnchorTarget.TargetNewWindow);
		anchor.setToolTip("Opens original image in a new tab or window");
	}

	private void initializeButtonPanel() {
		buttonContainer = new WContainerWidget();
		buttonContainer.setMaximumSize(new WLength(ImagePreviewWidget.IMAGE_WIDTH, WLength.Unit.Pixel), WLength.Auto);

		WHBoxLayout layout = new WHBoxLayout();

		annotateButton = new WPushButton("Annotate Image");
		annotateButton.setMargin(new WLength(10), EnumSet.of(Side.Right));
		annotateButton.clicked().addListener(this, () -> performShowAnnotationContainerAction());
		layout.addWidget(annotateButton);

		WPushButton deleteButton = new WPushButton("Delete Image");
		deleteButton.clicked().addListener(this, () -> performDeleteImageAction());

		layout.addWidget(deleteButton);
		buttonContainer.setLayout(layout);
	}

	private void initializeLayout() {
		WVBoxLayout layout = new WVBoxLayout();
		setLayout(layout);

		layout.addWidget(imagePreviewWidget);
		layout.addWidget(imageInformation);
		layout.addWidget(anchor);
		layout.addWidget(buttonContainer);
	}

	private void initializeDimensions() {
		final WLength width = new WLength(ImagePreviewWidget.IMAGE_WIDTH, WLength.Unit.Pixel);
		final WLength height = new WLength(ImagePreviewWidget.IMAGE_HEIGHT + LINK_HEIGHT, WLength.Unit.Pixel);
		resize(width, height);
		setMinimumSize(width, height);
		setMaximumSize(width, height);
	}

	private void performShowAnnotationContainerAction() {
		LOGGER.info("Triggering annotation page for image {}", image.getRelativeImagePath());

		// capture the measurements of the image - we need that to size the annotation page soon
		image.setWidth(imagePreviewWidget.getOriginalImageWidth());
		image.setHeight(imagePreviewWidget.getOriginalImageHeight());

		propertyChangeSupport.firePropertyChange(IMAGE_SELECTED_PROPERTY_NAME, null, image);
	}

	private void performDeleteImageAction() {
		WMessageBox messageBox = new WMessageBox("Confirmation", "Do you want to delete this image and all associated annotations now?", Icon.Question, EnumSet.of(StandardButton.Yes, StandardButton.No));
		messageBox.buttonClicked().addListener(this, () -> deleteConfirmationButtonAction(messageBox));
		messageBox.show();
	}

	private void deleteConfirmationButtonAction(WMessageBox messageBox) {
		if(messageBox.getButtonResult() == StandardButton.Yes) {
			// delete from ontology
			getImageAnnotationAPI().removeImage(getSessionID(), image.getIri());
			// delete all files
			((ImageFileService)getParentContainer().getFileService()).deleteImage(image.getRelativeImagePath());

			propertyChangeSupport.firePropertyChange(IMAGE_DELETED_PROPERTY_NAME, image, null);
		}

		messageBox.remove();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
