package no.siriuslabs.image;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGroupBox;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.EnumSet;

public class ImageSelectionContainer extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageSelectionContainer.class);

	private final FrontendApplication application;

	public ImageSelectionContainer(FrontendApplication application, WContainerWidget parent) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;

		WVBoxLayout layout = new WVBoxLayout();

		final String basePath = application.getServletContext().getRealPath("./");
		final String imagePath = basePath + FileService.IMAGE_DIRECTORY_NAME;

		File imageDir = new File(imagePath);

		// TODO replace GroupBox gimmick with grouping by image type after Ontology data is available
		int i = 1;
		WGroupBox groupBox = new WGroupBox("Group " + i);
		layout.addWidget(groupBox);

		// TODO replace iteration over image files with collection of images from Ontology and access images directly
		for (final File fileEntry : imageDir.listFiles()) {
			LOGGER.info(fileEntry.getName());

			String path = fileEntry.getPath().substring(fileEntry.getPath().indexOf(basePath) + basePath.length());
			String absolutePath = fileEntry.getAbsolutePath();
			PreviewSelectionWidget previewWidget = new PreviewSelectionWidget(this, path, absolutePath);
			previewWidget.setMargin(new WLength(50), EnumSet.of(Side.Bottom));
			if(i % 2 == 0) {
				groupBox = new WGroupBox("Group " + i);
				groupBox.setMargin(new WLength(50), EnumSet.of(Side.Bottom));
				layout.addWidget(groupBox);
			}
			groupBox.addWidget(previewWidget);

			i++;
		}

		setLayout(layout);
		setOverflow(Overflow.OverflowAuto);

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	protected FrontendApplication getApplication() {
		return application;
	}
}
