package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.KeyboardModifier;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WFileResource;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WImage;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.FrontendServlet;

import java.util.EnumSet;

/**
 * Container class representing the home/welcome screen.
 */
public class HomeContainer extends AbstractAnnotationContainer {

	private static final String LOGO_PATH = "/resources/sirius-logo-clean400-1.png";
	private static final String APPLICATION_TITLE = "GeoAnnotator";

	/**
	 * Constructor taking the application and parent container.
	 */
	public HomeContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);

		initializeContents();
	}

	private void initializeContents() {
		WHBoxLayout layout = new WHBoxLayout();

		addSpacerWidget(layout);

		final WImage logoWidget = new WImage(LOGO_PATH);
		if(Boolean.TRUE.equals(getApplication().getServletContext().getAttribute(FrontendServlet.EXTERNAL_DIRECTORY_SET))) {
			// we only enable data download if an external data directory is used
			logoWidget.clicked().addListener(this, (WMouseEvent arg) -> logoClickedAction(arg));
		}
		layout.addWidget(logoWidget, 0, EnumSet.of(AlignmentFlag.AlignMiddle, AlignmentFlag.AlignCenter));

		final WText titleText = new WText("<h1>" + APPLICATION_TITLE + "</h1>");
		layout.addWidget(titleText, 0, EnumSet.of(AlignmentFlag.AlignMiddle, AlignmentFlag.AlignCenter));

		addSpacerWidget(layout);

		setLayout(layout);
	}

	private void addSpacerWidget(WHBoxLayout layout) {
		layout.addWidget(new WText(""), 1);
	}

	private void logoClickedAction(WMouseEvent arg) {
		if(arg.getModifiers().contains(KeyboardModifier.ShiftModifier)) {
			final String zipFile = getFileService().dumpDataDirectoryToZipArchive();

			WDialog dialog = new WDialog("Download Data");

			WFileResource fileResource = new WFileResource("application/zip", zipFile);
			fileResource.suggestFileName(zipFile);

			WAnchor anchor = new WAnchor(new WLink(fileResource), "Download now");
			anchor.setPadding(new WLength(20), Side.Bottom);
			anchor.setTarget(AnchorTarget.TargetDownload);

			WPushButton closeButton = new WPushButton("Close");
			closeButton.clicked().addListener(dialog, (WMouseEvent arg1) -> {
				dialog.hide();
				getFileService().deleteFile(zipFile);
			});

			WVBoxLayout dialogLayout = new WVBoxLayout();
			dialogLayout.addWidget(anchor);
			dialogLayout.addWidget(closeButton);
			dialog.getContents().setLayout(dialogLayout);

			dialog.show();
		}
	}
}
