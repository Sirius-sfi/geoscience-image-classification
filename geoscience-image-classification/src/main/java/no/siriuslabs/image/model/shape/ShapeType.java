package no.siriuslabs.image.model.shape;

/**
 * Enum representing the different possible shapes and the number of points they can or must have.
 */
public enum ShapeType {

	/**
	 * Circle is a special case that must have two points, but can be created with only one. The other will be added later.
	 */
	CIRCLE(2),
	TRIANGLE(3),
	RECTANGLE(4),
	/**
	 * For the Polygon 5 is the minimum number of points.
	 */
	POLYGON(5);

	private final int numberOfPoints;

	ShapeType(int numberOfPoints) {
		this.numberOfPoints = numberOfPoints;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}
}
