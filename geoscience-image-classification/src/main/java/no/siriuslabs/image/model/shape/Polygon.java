package no.siriuslabs.image.model.shape;

import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Shape-class representing a polygon with 5+ points.
 */
public class Polygon extends AbstractShape {

	private static final Logger LOGGER = LoggerFactory.getLogger(Polygon.class);

	/**
	 * Constructor expecting the minimum 5 points defining the polygon.
	 */
	public Polygon(List<WPointF> points) {
		super(ShapeType.POLYGON, points);
	}

	/**
	 * Checks if the number of points matches the ShapeType and throw an IllegalArgumentException if not.
	 */
	@Override
	protected void checkPointsParameter(ShapeType shapeType, List<WPointF> points) {
		if(points.isEmpty() || points.size() < shapeType.getNumberOfPoints()) {
			throw new IllegalArgumentException(shapeType.name() + " must have a minimum of 5 points.");
		}
	}

	@Override
	public void drawYourself(WPainterPath path) {
		logPoints(LOGGER);
		path.addPolygon(getPointsToDraw());
	}
}
