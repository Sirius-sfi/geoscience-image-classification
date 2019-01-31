package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.ui.container.ImageSelectionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Widget combining an image preview, a link to the full size image and a selection button.
 */
public class PreviewSelectionWidget extends AbstractAnnotationWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewSelectionWidget.class);

	public static final String IMAGE_SELECTED_PROPERTY_NAME = "previewSelectionWidget.imageSelected";

	private static final int LINK_HEIGHT = 50;

	private final GeologicalImage image;

	private final PropertyChangeSupport propertyChangeSupport;

	private ImagePreviewWidget imagePreviewWidget;
	private WAnchor anchor;
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

		initializeLink(image.getRelativeImagePath());
		initializeAnnotateButton();

		initializeLayout();
		initializeDimensions();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializePreviewWidget() {
		imagePreviewWidget = new ImagePreviewWidget(image.getRelativeImagePath(), image.getAbsoluteImagePath());
	}

	private void initializeLink(String path) {
		WLink link = new WLink(path);
		anchor = new WAnchor(link, image.getLabel() + " (view full size)");
		anchor.setTarget(AnchorTarget.TargetNewWindow);
		anchor.setToolTip("Opens original image in a new tab or window");
	}

	private void initializeAnnotateButton() {
		annotateButton = new WPushButton("Annotate Image");
		annotateButton.setMaximumSize(new WLength(ImagePreviewWidget.IMAGE_WIDTH, WLength.Unit.Pixel), WLength.Auto);
		annotateButton.clicked().addListener(this, () -> performShowAnnotationContainerAction());
	}

	private void initializeLayout() {
		WVBoxLayout layout = new WVBoxLayout();
		setLayout(layout);

		layout.addWidget(imagePreviewWidget);
		layout.addWidget(anchor);
		layout.addWidget(annotateButton);
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

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
