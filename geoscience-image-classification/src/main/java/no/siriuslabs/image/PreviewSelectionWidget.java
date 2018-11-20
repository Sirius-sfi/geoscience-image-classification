package no.siriuslabs.image;

import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWidget;
import no.siriuslabs.image.model.GeologicalImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewSelectionWidget extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewSelectionWidget.class);

	private static final int LINK_HEIGHT = 50;

	private final GeologicalImage image;

	private ImagePreviewWidget imagePreviewWidget;
	private WAnchor anchor;
	private WPushButton annotateButton;

	public PreviewSelectionWidget(ImageSelectionContainer parent, GeologicalImage image) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.image = image;
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
		LOGGER.info("Displaying annotation page for image {}", image.getRelativeImagePath());
		WWidget groupBox = getParent();
		ImageSelectionContainer imageSelectionContainer = (ImageSelectionContainer) groupBox.getParent();
		imageSelectionContainer.getApplication().showAnnotationContainer(image);
	}
}
