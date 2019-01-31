package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.WContainerWidget;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.ui.container.AbstractAnnotationContainer;

/**
 * Abstract widget class which provides common basic functionality such as access to API and parent container for widgets.
 */
public abstract class AbstractAnnotationWidget extends WContainerWidget {

	private final AbstractAnnotationContainer parentContainer;

	/**
	 * Constructor taking the parent container.
	 */
	protected AbstractAnnotationWidget(AbstractAnnotationContainer parent) {
		super(parent);
		parentContainer = parent;
	}

	/**
	 * Returns the parent container of this widget.
	 */
	protected AbstractAnnotationContainer getParentContainer() {
		return parentContainer;
	}

	/**
	 * Returns the ontology session ID.
	 */
	protected String getSessionID() {
		return parentContainer.getSessionID();
	}

	// TODO API method should be "de-imaged"
	/**
	 * Returns the sessions's API service instance.
	 */
	protected ImageAnnotationAPI getImageAnnotationAPI() {
		return parentContainer.getImageAnnotationAPI();
	}
}
