package no.siriuslabs.image.model.shape;

import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract superclass of all shapes.
 * It holds a ShapeType and a list of points defining the shape.
 */
public abstract class AbstractShape {

	private final ShapeType shapeType;
	private final List<WPointF> points;
	private final List<WPointF> pointsToDraw;

	/**
	 * Constructor taking the type of the shape and the list of points defining the shape.
	 */
	protected AbstractShape(ShapeType shapeType, List<WPointF> points) {
		checkPointsParameter(shapeType, points);

		this.shapeType = shapeType;
		this.points = new ArrayList<>(points);
		pointsToDraw = new ArrayList<>(points.size());

		initializePointsToDraw();
	}

	/**
	 * Checks if the number of points matches the ShapeType and throw an IllegalArgumentException if not.
	 */
	protected void checkPointsParameter(ShapeType shapeType, List<WPointF> points) {
		if(points.isEmpty() || points.size() != shapeType.getNumberOfPoints()) {
			throw new IllegalArgumentException(shapeType.name() + " must have " + shapeType.getNumberOfPoints() + " points.");
		}
	}

	/**
	 * Constructor taking only the type of the shape. The points defining the shape have to be provided via the initializePoints() method afterwards.
	 * This is intended for shapes that need to modify the points list after object construction.
	 */
	protected AbstractShape(ShapeType shapeType) {
		this.shapeType = shapeType;
		points = new ArrayList<>();
		pointsToDraw = new ArrayList<>();
	}

	/**
	 * Replaces the current contents of the points list with the one given and accordingly re-initializes the pointsToDraw.
	 */
	protected void initializePoints(List<WPointF> points) {
		checkPointsParameter(shapeType, points);

		this.points.clear();
		this.points.addAll(points);

		initializePointsToDraw();
	}

	private void initializePointsToDraw() {
		pointsToDraw.clear();
		pointsToDraw.addAll(points);

		switch(shapeType) {
			case TRIANGLE:
			case POLYGON: {
				// we have a triangle or polygon and should close it to enable a complete drawing
				pointsToDraw.add(points.get(0));
				break;
			}
			default: {
				// do nothing
			}
		}
	}

	/**
	 * Add the points to draw the shape to the given PainterPath.
	 */
	public abstract void drawYourself(WPainterPath path);

	/**
	 * Returns the type of the shape.
	 */
	public ShapeType getShapeType() {
		return shapeType;
	}

	/**
	 * Returns the list of points that define the shape.
	 */
	public List<WPointF> getPoints() {
		return Collections.unmodifiableList(points);
	}

	/**
	 * Returns the list of points that are used to draw the shape.
	 * This list might differ from the points defining the shape, depending of the type of shape to be drawn.
	 */
	public List<WPointF> getPointsToDraw() {
		return Collections.unmodifiableList(pointsToDraw);
	}

	/**
	 * Convenience method to log all points of a shape.
	 */
	protected void logPoints(Logger logger) {
		for(WPointF point : getPoints()) {
			logger.info("point {}:{}", point.getX(), point.getY());
		}
	}
}
