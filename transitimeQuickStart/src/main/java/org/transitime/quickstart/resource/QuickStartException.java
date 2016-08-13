package org.transitime.quickstart.resource;

import org.transitime.gui.ExceptionPanel;

public class QuickStartException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QuickStartException(String message, Exception ex) {
		super(message, ex);
		ExceptionPanel exception = new ExceptionPanel(message, ex);
	}

}
