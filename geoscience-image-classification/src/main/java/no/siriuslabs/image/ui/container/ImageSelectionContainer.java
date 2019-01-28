package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGroupBox;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.FrontendServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.services.ImageFileService;
import no.siriuslabs.image.ui.widget.PreviewSelectionWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;
import java.util.List;

/**
 * Container class representing the image selection page.
 */
public class ImageSelectionContainer extends AbstractAnnotationContainer implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageSelectionContainer.class);

	public static final String START_ANNOTATING_PROPERTY_NAME = "imageSelectionContainer.startAnnotating";

	private final PropertyChangeSupport propertyChangeSupport;

	/**
	 * Constructor taking the application and the parent container.
	 */
	public ImageSelectionContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		propertyChangeSupport = new PropertyChangeSupport(this);

		WVBoxLayout layout = new WVBoxLayout();

		ImageFileService fileService = (ImageFileService) getFileService();

		String sessionID = (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
		String type = "Geological image";

		List<GeologicalImage> images = ((ImageAnnotationAPI)application.getServletContext().
				getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY)).getImagesOfGivenType(sessionID, type);
		

		WGroupBox groupBox = new WGroupBox();
		layout.addWidget(groupBox);

		for(GeologicalImage image : images) {
			LOGGER.info("setting up image: {}", image.getName());

			// TODO initialize in IA-API?
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

		setLayout(layout);
		setOverflow(Overflow.OverflowAuto);

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
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
