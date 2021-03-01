package org.transitclock.exceptions;

public class InvalidRouteException extends Exception{
    public InvalidRouteException(String route){
        super("Invalid route " + route);
    }
}
