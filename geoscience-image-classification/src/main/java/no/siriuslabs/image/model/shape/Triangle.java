package no.siriuslabs.image.model.shape;

import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Shape-class representing a triangle.
 */
public class Triangle extends AbstractShape {

	private static final Logger LOGGER = LoggerFactory.getLogger(Triangle.class);

	/**
	 * Constructor expecting the three points defining the triangle.
	 */
	public Triangle(List<WPointF> points) {
		super(ShapeType.TRIANGLE, points);
	}

	@Override
	public void drawYourself(WPainterPath path) {
		logPoints(LOGGER);
		path.addPolygon(getPointsToDraw());
	}
}
