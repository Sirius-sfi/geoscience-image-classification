package no.siriuslabs.image;

import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewSelectionWidget extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewSelectionWidget.class);

	private static final int LINK_HEIGHT = 50;

	private final String imagePath;
	private final String imageAbsoluteFilePath;

	private ImagePreviewWidget imagePreviewWidget;
	private WAnchor anchor;
	private WPushButton annotateButton;

	public PreviewSelectionWidget(ImageSelectionContainer parent, String path, String absoluteFilePath) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		imagePath = path;
		imageAbsoluteFilePath = absoluteFilePath;
		initializePreviewWidget(imagePath, imageAbsoluteFilePath);

		initializeLink(path);
		initializeAnnotateButton();

		initializeLayout();
		initializeDimensions();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializePreviewWidget(String path, String absoluteFilePath) {
		imagePreviewWidget = new ImagePreviewWidget(path, absoluteFilePath);
	}

	private void initializeLink(String path) {
		WLink link = new WLink(path);
		// TODO display the image name from the Ontology here instead of the path
		anchor = new WAnchor(link, path);
		anchor.setTarget(AnchorTarget.TargetNewWindow);
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
		LOGGER.info("Displaying annotation page for image {}", imagePath);
		WWidget groupBox = getParent();
		ImageSelectionContainer imageSelectionContainer = (ImageSelectionContainer) groupBox.getParent();
		imageSelectionContainer.getApplication().showAnnotationContainer(imagePath, imageAbsoluteFilePath);
	}
}
