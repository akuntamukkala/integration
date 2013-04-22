package com.vpn.integration.route.rfq.exception;

/**
 * Exception thrown when the downstream endpoint is unavailable
 * 
 * @TODO
 *	The discount pricing could be obtained from a web service endpoint. At which point, there is a possibility
 *	that the web service end point is unavailable. This exception can be thrown in such a case. 
 * 
 * @author AKUNTAMU
 *
 */
public class EndpointUnavailableException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8197713941832332417L;

}
