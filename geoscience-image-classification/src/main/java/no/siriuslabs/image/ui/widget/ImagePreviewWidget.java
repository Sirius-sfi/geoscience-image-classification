package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.WPaintDevice;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WRectF;

public class ImagePreviewWidget extends WPaintedWidget {

	protected static final int IMAGE_WIDTH = 600;
	protected static final int IMAGE_HEIGHT = 600;

	private final String path;
	private final String absoluteFilePath;

	private int originalImageWidth = -1;
	private int originalImageHeight = -1;

	public ImagePreviewWidget(String path, String absoluteFilePath) {
		this.path = path;
		this.absoluteFilePath = absoluteFilePath;

		resize(IMAGE_WIDTH, IMAGE_HEIGHT);
	}

	@Override
	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		WPainter.Image image = new WPainter.Image(path, absoluteFilePath);

		// if not yet set, we keep the measurements of the image for later use
		if(originalImageWidth == -1) {
			originalImageWidth = image.getWidth();
			originalImageHeight = image.getHeight();
		}

		WRectF destinationRect = new WRectF(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
		painter.drawImage(destinationRect, image);
	}

	/**
	 * Returns the original, unzoomed and unchanged width of the image.
	 */
	protected int getOriginalImageWidth() {
		return originalImageWidth;
	}

	/**
	 * Returns the original, unzoomed and unchanged height of the image.
	 */
	protected int getOriginalImageHeight() {
		return originalImageHeight;
	}
}
