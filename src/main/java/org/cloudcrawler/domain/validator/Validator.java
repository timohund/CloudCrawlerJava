package org.cloudcrawler.domain.validator;

/**
 * Interface that should be implemented by a validator.
 *
 */
public interface Validator {

    public boolean isValid(Object object);
}
