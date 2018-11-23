package no.siriuslabs.image.model.shape;

import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Shape-class representing a circle.
 * This circle can either be based on two points (center and radius) or one point, in which case it will be given a default radius.
 */
public class Circle extends AbstractShape {

	private static final Logger LOGGER = LoggerFactory.getLogger(Circle.class);

	/**
	 * Default radius for one-point-based circles.
	 */
	public static final int DEFAULT_RADIUS = 20;

	private final boolean hasCustomRadius;

	/**
	 * Constructor expecting either one or two points to make a circle.
	 */
	public Circle(List<WPointF> points) {
		super(ShapeType.CIRCLE);

		hasCustomRadius = points.size() != 1;
		if(!hasCustomRadius) {
			// if the circle is defined only by one point, we add another with the default radius
			WPointF center = points.get(0);
			WPointF radius = new WPointF(center.getX(), center.getY() + DEFAULT_RADIUS);
			points.add(radius);
		}

		initializePoints(points);
	}

	/**
	 * Checks if the number of points matches the ShapeType and throw an IllegalArgumentException if not.
	 */
	@Override
	protected void checkPointsParameter(ShapeType shapeType, List<WPointF> points) {
		if(points.isEmpty() || points.size() > 2) {
			throw new IllegalArgumentException(shapeType.name() + " must have 1 or 2 points.");
		}
	}

	@Override
	public void drawYourself(WPainterPath path) {
		logPoints(LOGGER);

		WPointF center = getCenter();
		LOGGER.info("center={}:{}", center.getX(), center.getY());

		double radius = getRadius();
		LOGGER.info("radius={}", radius);

		// draws an ellipse based on the top left corner of the circle's bounding box
		path.addEllipse(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2);
	}

	/**
	 * Return the center point of the circle.
	 */
	public WPointF getCenter() {
		return getPoints().get(0);
	}

	/**
	 * Returns the radius of the circle.
	 */
	public double getRadius() {
		WPointF center = getCenter();
		WPointF radiusPoint = getPoints().get(1);
		LOGGER.info("radius point={}:{}", radiusPoint.getX(), radiusPoint.getY());

		// based on formula: sqrt(distanceX e2  + distanceY e2)
		double xDist = center.getX() - radiusPoint.getX();
		double yDist = center.getY() - radiusPoint.getY();

		double xDistSquared = Math.pow(xDist, 2);
		double yDistSquared = Math.pow(yDist, 2);

		double sumOfDistances = xDistSquared + yDistSquared;
		return Math.sqrt(sumOfDistances);
	}

	/**
	 * Returns true if the circle is based on two points (center + radius) and false, if it is based on the center only.
	 */
	public boolean hasCustomRadius() {
		return hasCustomRadius;
	}
}
