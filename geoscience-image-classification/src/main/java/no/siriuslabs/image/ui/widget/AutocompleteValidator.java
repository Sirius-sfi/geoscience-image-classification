package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.WValidator;

/**
 * Validator implementation for single element auto-complete fields.
 */
public class AutocompleteValidator extends WValidator {

	/**
	 * Constructor taking a flag if the field should be validated as mandatory.
	 */
	public AutocompleteValidator(boolean mandatory) {
		super(mandatory);
	}

	@Override
	public Result validate(String input) {
		// perform standard validation
		Result result = super.validate(input);
		if(!(State.Valid == result.getState())) {
			return result;
		}

		// validate if only one auto-complete element is present
		if(input.indexOf(',') != input.lastIndexOf(',')) {
			return new Result(State.Invalid, "Only one element is allowed");
		}

		return new Result(State.Valid);
	}
}
