package it.polimi.astalavista.exceptions;

public class CountryNotAvailableException extends RuntimeException {
    public CountryNotAvailableException() {
        super("Selected country is not available.");
    }
}
