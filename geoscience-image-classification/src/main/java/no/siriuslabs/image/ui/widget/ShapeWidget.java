package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.PaintFlag;
import eu.webtoolkit.jwt.PenCapStyle;
import eu.webtoolkit.jwt.PenJoinStyle;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPaintDevice;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.WPointF;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.FrontendServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.shape.AbstractShape;
import no.siriuslabs.image.model.shape.Circle;
import no.siriuslabs.image.model.shape.Polygon;
import no.siriuslabs.image.model.shape.Rectangle;
import no.siriuslabs.image.model.shape.Triangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Widget that displays and image and allows shapes to be drawn on the image.
 */
public class ShapeWidget extends WPaintedWidget {

	public static final String WIDGET_MODE_PROPERTY_NAME = "shapeWidget.widgetMode";

	/**
	 * Represents the current mode of operation of the ShapeWidget.
	 */
	public enum AnnotationWidgetMode {
		/**
		 * Set points to define a shape. Every click sets another point.
		 */
		SET_POINTS,
		/**
		 * Shape is marked as done by the user but not yet saved. Temporary points and shape are both displayed.
		 */
		UNSAVED_SHAPE,
		/**
		 * Shape was saved, temporary markers were removed.
		 */
		SAVED_SHAPE;
	}

	/**
	 * Represents a zoom level of an image.
	 * The order of the elements must be in the correct order of magnification to work correctly.
	 */
	public enum ZoomLevel {
		X1_0(1.0d),
		X1_5(1.5d),
		X2_0(2.0d),
		X3_0(3.0d);

		private final double magnification;

		ZoomLevel(double magnification) {
			this.magnification = magnification;
		}

		/**
		 * Returns the magnification of this level as a number.
		 */
		public double getMagnification() {
			return magnification;
		}

		/**
		 * Returns false if we are already at highest magnification, otherwise true.
		 */
		public boolean canIncrease() {
			return this != X3_0;
		}

		/**
		 * Returns false if we are already at standard (lowest) magnification, otherwise true.
		 */
		public boolean canDecrease() {
			return this != X1_0;
		}

		/**
		 * Returns the next higher zoom level to the given one or the highest if this is already reached.
		 */
		public static ZoomLevel findNextLevel(ZoomLevel level) {
			if(level.canIncrease()) {
				int next = level.ordinal() +1;
				return ZoomLevel.values()[next];
			}

			return level;
		}

		/**
		 * Returns the next lower zoom level to the given one or the lowest if this is already reached.
		 */
		public static ZoomLevel findPreviousLevel(ZoomLevel level) {
			if(level.canDecrease()) {
				int prev = level.ordinal() -1;
				return ZoomLevel.values()[prev];
			}

			return level;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeWidget.class);

	private final AbstractAnnotationApplication application;
	private final GeologicalImage image;

	private final PropertyChangeSupport propertyChangeSupport;

	private WPainterPath path;

	private ZoomLevel currentZoomLevel = ZoomLevel.X1_0;

	/**
	 * List of points in the image which will define the shape when it is confirmed.
	 */
	private List<WPointF> points = new ArrayList<>();

	/**
	 * List of shapes present for this image. Loaded from and saved to ontology.
	 */
	private List<AbstractShape> shapes = new ArrayList<>();

	/**
	 * Current shape between confirmation and saving.
	 */
	private AbstractShape unsavedShape = null;

	private AnnotationWidgetMode widgetMode = AnnotationWidgetMode.SAVED_SHAPE;

	/**
	 * Constructor taking the application and the GeologicalImage to be displayed.
	 */
	public ShapeWidget(AbstractAnnotationApplication application, GeologicalImage image) {
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;
		this.image = image;

		path = new WPainterPath();
		propertyChangeSupport = new PropertyChangeSupport(this);

		loadShapes();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	/**
	 * Cancels the points entered for the current shape and resets the shape definition.
	 */
	public void resetShape() {
		LOGGER.info("resetting points");

		setWidgetMode(AnnotationWidgetMode.SAVED_SHAPE);
		points.clear();
		path = new WPainterPath();
		update();
	}

	/**
	 * Adds the coordinates from the event parameter as another point and paints a temporary marker for it.
	 */
	public void addPointToShape(WMouseEvent arg) {
		// take coordinates from mouse click and modify by current magnification level --> point of click in 100% zoom
		double xCoordinate = arg.getWidget().x / currentZoomLevel.getMagnification();
		double yCoordinate = arg.getWidget().y / currentZoomLevel.getMagnification();

		LOGGER.info("adding point {}:{}", xCoordinate, yCoordinate);

		final WPointF point = new WPointF(xCoordinate, yCoordinate);
		// add point of click to list of points for the current shape
		points.add(point);

		// draw a marker around the point of click
		final int markerRadius = 10;
		path.addEllipse(point.getX()-markerRadius, point.getY()-markerRadius, markerRadius *2, markerRadius *2);

		update(EnumSet.of(PaintFlag.PaintUpdate));
	}

	/**
	 * Confirms the current collection of points and tries to construct a shape from them.
	 * If successful, the shape will be drawn and the widgetMode changed.
	 */
	public void confirmShape() {
		int numberOfPoints = points.size();
		LOGGER.info("creating shape - numberOfPoints={}", numberOfPoints);

		AbstractShape shape = null;

		switch(numberOfPoints) {
			case 0: {
				LOGGER.info("no points --> do nothing");
				return;
			}
			case 1: {
				LOGGER.info("{} points --> it's a default circle!", numberOfPoints);
				shape = new Circle(points);
				break;
			}
			case 2: {
				LOGGER.info("{} points --> it's a circle with custom radius!", numberOfPoints);
				shape = new Circle(points);
				break;
			}
			case 3: {
				LOGGER.info("{} points --> it's a triangle!", numberOfPoints);
				shape = new Triangle(points);
				break;
			}
			case 4: {
				LOGGER.info("{} points --> it's a rectangle!", numberOfPoints);
				shape = new Rectangle(points);
				break;
			}
			default: {
				LOGGER.info("{} points --> it's a polygon!", numberOfPoints);
				shape = new Polygon(points);
				break;
			}
		}

		shape.drawYourself(path);
		unsavedShape = shape;

		setWidgetMode(AnnotationWidgetMode.UNSAVED_SHAPE);

		points.clear();
		update(EnumSet.of(PaintFlag.PaintUpdate));
	}

	/**
	 * Reacts to saving the current shape to the ontology.
	 */
	public void handleShapeSaved() {
		LOGGER.info("saving shape");

		shapes.add(unsavedShape);

		setWidgetMode(AnnotationWidgetMode.SAVED_SHAPE);
		unsavedShape = null;
		path = new WPainterPath();
		update();
	}

	/**
	 * Loads existing shapes for this image from the ontology.
	 */
	private void loadShapes() {
		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();
		Set<AbstractShape> shapeSet = imageAnnotationAPI.getSelectionShapesForImage(sessionID, image.getIri());

		shapes.clear();
		shapes.addAll(shapeSet);
	}

	@Override
	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		WPainter.Image paintedImage = new WPainter.Image(image.getRelativeImagePath(), image.getAbsoluteImagePath());

		// scale the image to the zoom level set by the user
		painter.scale(currentZoomLevel.getMagnification(), currentZoomLevel.getMagnification());

		painter.drawImage(new WPointF(0, 0), paintedImage);


		// TODO how to change painters back and forth?
//		WPen tempPen = createPlaceholderPen();
//		painter.setPen(tempPen);
//		painter.drawPath(path);
//
//
//		if(!shapes.isEmpty()) {
			WPen permanentPen = createPermanentPen();
			painter.setPen(permanentPen);

			for(AbstractShape shape : shapes) {
				shape.drawYourself(path);
			}
			painter.drawPath(path);
//		}
	}

	private WPen createPermanentPen() {
		WPen pen = new WPen();
		pen.setWidth(new WLength(2));
		pen.setColor(WColor.darkMagenta);
		pen.setCapStyle(PenCapStyle.FlatCap);
		pen.setJoinStyle(PenJoinStyle.MiterJoin);
		return pen;
	}

	private WPen createPlaceholderPen() {
		WPen pen = new WPen();
		pen.setWidth(new WLength(2));
		pen.setColor(WColor.cyan);
		pen.setCapStyle(PenCapStyle.RoundCap);
		pen.setJoinStyle(PenJoinStyle.BevelJoin);
		return pen;
	}

	private String getSessionID() {
		return (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
	}

	private ImageAnnotationAPI getImageAnnotationAPI() {
		return (ImageAnnotationAPI) application.getServletContext().getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY);
	}

	public void setWidgetMode(AnnotationWidgetMode mode) {
		AnnotationWidgetMode oldValue = widgetMode;
		widgetMode = mode;
		propertyChangeSupport.firePropertyChange(WIDGET_MODE_PROPERTY_NAME, oldValue, widgetMode);
	}

	public AnnotationWidgetMode getWidgetMode() {
		return widgetMode;
	}

	public AbstractShape getUnsavedShape() {
		return unsavedShape;
	}

	/**
	 * Increases the image's zoom level by one if possible.
	 */
	public void increaseZoomLevel() {
		if(currentZoomLevel.canIncrease()) {
			currentZoomLevel = ZoomLevel.findNextLevel(currentZoomLevel);
			resize(new WLength(image.getWidth() * currentZoomLevel.getMagnification()), new WLength(image.getHeight() * currentZoomLevel.getMagnification()));
		}
	}

	/**
	 * Decreases the image's zoom level by one if possible.
	 */
	public void decreaseZoomLevel() {
		if(currentZoomLevel.canDecrease()) {
			currentZoomLevel = ZoomLevel.findPreviousLevel(currentZoomLevel);
			resize(new WLength(image.getWidth() * currentZoomLevel.getMagnification()), new WLength(image.getHeight() * currentZoomLevel.getMagnification()));
		}
	}

	/**
	 * Resets the image's zoom level to standard.
	 */
	public void resetZoomLevel() {
		currentZoomLevel = ZoomLevel.X1_0;
		resize(new WLength(image.getWidth() * currentZoomLevel.getMagnification()), new WLength(image.getHeight() * currentZoomLevel.getMagnification()));
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
