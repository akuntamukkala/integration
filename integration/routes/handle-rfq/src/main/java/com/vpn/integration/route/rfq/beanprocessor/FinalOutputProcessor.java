package com.vpn.integration.route.rfq.beanprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.thoughtworks.xstream.XStream;
import com.vpn.integration.route.rfq.vo.RFQ;

public class FinalOutputProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		XStream xstream = new XStream();
		//xstream.processAnnotations(RFQ.class);
		System.out.println("Final Output = " + (String) exchange.getIn().getBody());
	}

}
