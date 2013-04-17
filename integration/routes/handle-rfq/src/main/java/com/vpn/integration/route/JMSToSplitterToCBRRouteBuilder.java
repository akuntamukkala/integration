package com.vpn.integration.route;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.model.RedeliveryPolicyDefinition;
import org.apache.camel.processor.RedeliveryPolicy;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.camel.spring.spi.TransactionErrorHandler;
import org.springframework.jms.connection.JmsTransactionManager;

import com.vpn.integration.route.rfq.aggregation.RFQResponseAggregationStrategy;
import com.vpn.integration.route.rfq.beanprocessor.DramaProcessor;
import com.vpn.integration.route.rfq.beanprocessor.FictionProcessor;
import com.vpn.integration.route.rfq.beanprocessor.FinalOutputProcessor;
import com.vpn.integration.route.rfq.beanprocessor.RedeliveryDelayProcessor;
import com.vpn.integration.route.rfq.exception.BadMessageException;
import com.vpn.integration.route.rfq.exception.EndpointUnavailableException;
import com.vpn.integration.route.rfq.exception.RFQRuntimeException;

public class JMSToSplitterToCBRRouteBuilder extends SpringRouteBuilder {

	private String outputFileDirectory;
	
	public String getOutputFileDirectory() {
		return outputFileDirectory;
	}

	public void setOutputFileDirectory(String outputFileDirectory) {
		this.outputFileDirectory = outputFileDirectory;
	}

	@Override
	public void configure() throws Exception {

		XPathBuilder xPathBuilder = new XPathBuilder("//rfq/books/item"); 
		
		JmsTransactionManager mgr = (JmsTransactionManager) getContext().getRegistry().lookup("txManager");

		onException(BadMessageException.class).handled(true).to("jms:bad-msg-format");
		
//		RedeliveryPolicyDefinition unlmtdRtry = new RedeliveryPolicyDefinition();
//		unlmtdRtry.setMaximumRedeliveries("-1");
//		unlmtdRtry.setRedeliveryDelay("10000");
//		unlmtdRtry.setUseExponentialBackOff("false");
		
		//errorHandler(transactionErrorHandler(mgr).maximumRedeliveries(-1).redeliveryDelay(10000));
		
		//onException(EndpointUnavailableException.class).handled(false).maximumRedeliveries(-1).redeliveryDelay(10000).log("Exception Occurred"); //.setRedeliveryPolicy(unlmtdRtry);
		
		from("jms:rfq?consumer.prefetchSize=0")
				.id("queue:rfq->split->cbr->queue:[DRAMA|FICTION]")
				.transacted("required").process(new RedeliveryDelayProcessor())
				.split(xPathBuilder, new RFQResponseAggregationStrategy()).parallelProcessing().timeout(3000)
					.to("direct:cbr")
				.end()
				.setHeader("CamelFileName", simple("${header.rfqId}" + "-output.xml"))
				.to("file://" + this.outputFileDirectory);
				
		
		from("direct:cbr").setHeader("itemType",
				XPathBuilder.xpath("//item/@type", String.class))
		.choice()
			.when(header("itemType").isEqualTo("FICTION")).convertBodyTo(String.class).process(new FictionProcessor())
			.when(header("itemType").isEqualTo("DRAMA")).convertBodyTo(String.class).process(new DramaProcessor())
			.otherwise().to("jms:UnknownType");
		
	}
}
