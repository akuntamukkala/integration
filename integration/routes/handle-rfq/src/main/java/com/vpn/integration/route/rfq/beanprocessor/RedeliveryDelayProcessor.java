package com.vpn.integration.route.rfq.beanprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * This processor is to help implement redelivery delay in case of an exception situation where the message 
 * needs to be retried indefinitely and sleep for duration of 10000ms between the retries.
 * @TODO
 * 	Make sleep time configurable 
 * @author AKUNTAMU
 *
 */
public class RedeliveryDelayProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		if ((Boolean) ((org.apache.camel.component.jms.JmsMessage) exchange
						.getIn()).getHeader("JMSRedelivered")) {
			Thread.sleep(10000);
		}

	}

}
