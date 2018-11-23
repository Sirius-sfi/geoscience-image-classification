package no.siriuslabs.image.model.shape;

import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Shape-class representing a rectangle that is aligned to the X and Y axis.
 */
public class Rectangle extends AbstractShape {

	private static final Logger LOGGER = LoggerFactory.getLogger(Rectangle.class);

	/**
	 * Constructor expecting the four points defining the rectangle.
	 */
	public Rectangle(List<WPointF> points) {
		super(ShapeType.RECTANGLE);

		sortPoints(points);

		initializePoints(points);
	}

	/**
	 * Sorts the points list by position in the coordinate system (combined X and Y value).
	 */
	private void sortPoints(List<WPointF> points) {
		points.sort((WPointF o1, WPointF o2) -> {
			if(o1 == null) {
				return -1;
			}
			if(o2 == null) {
				return 1;
			}

			double o1Combined = o1.getX() + o1.getY();
			double o2Combined = o2.getX() + o2.getY();
			int resultCombined = Double.compare(o1Combined, o2Combined);

			if(resultCombined == 0) {
				int resultX = Double.compare(o1.getX(), o2.getX());
				if(resultX == 0) {
					return Double.compare(o1.getY(), o2.getY());
				}
				return resultX;
			}
			return resultCombined;
		});
	}

	@Override
	public void drawYourself(WPainterPath path) {
		logPoints(LOGGER);

		final WPointF leftUpperCorner = getPoints().get(0);
		final WPointF rightUpperCorner = getPoints().get(1);
		final WPointF leftLowerCorner = getPoints().get(2);
		final WPointF rightLowerCorner = getPoints().get(3);

		// we try to find the maximum extend of the points on X and Y axis...
		double maxExtendOnX = rightUpperCorner.getX() > rightLowerCorner.getX() ? rightUpperCorner.getX() : rightLowerCorner.getX();
		double maxExtendOnY = leftLowerCorner.getY() > rightLowerCorner.getY() ? leftLowerCorner.getY() : rightLowerCorner.getY();

		// ...and construct a rectangle that tries to include all or most of the points even for irregular cases
		path.addRect(leftUpperCorner.getX(), leftUpperCorner.getY(), maxExtendOnX - leftUpperCorner.getX(), maxExtendOnY - leftUpperCorner.getY());
	}

	/**
	 * Returns the point at the upper left corner of the rectangle.
	 */
	public WPointF getUpperLeftCorner() {
		return getPoints().get(0);
	}
}
