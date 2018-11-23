package no.siriuslabs.image;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGroupBox;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.shape.Circle;
import no.siriuslabs.image.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ImageSelectionContainer extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageSelectionContainer.class);

	private final FrontendApplication application;

	public ImageSelectionContainer(FrontendApplication application, WContainerWidget parent) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;

		WVBoxLayout layout = new WVBoxLayout();

		FileService fileService = (FileService) application.getServletContext().getAttribute(FrontendServlet.FILE_SERVICE_KEY);

		String sessionID = (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
		String type = "Geological image";

		List<GeologicalImage> images = ((ImageAnnotationAPI)application.getServletContext().
				getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY)).getImagesOfGivenType(sessionID, type);
		
		
		//TODO Test Ernesto
		for (GeologicalImage gimg : images) {
			
			List<WPointF> points = new ArrayList<WPointF>();
			
			points.add(new WPointF(0.1, 0.5));
			
			points.add(new WPointF(1.1, 1.5));
			
			Circle circle = new Circle(points);
			
			((ImageAnnotationAPI)application.getServletContext().
					getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY)).saveNewShape(sessionID, gimg.getIri(), circle);
			
		}	
		//TODO Test Ernesto. To be removed
		
		
		

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

	protected FrontendApplication getApplication() {
		return application;
	}
}
