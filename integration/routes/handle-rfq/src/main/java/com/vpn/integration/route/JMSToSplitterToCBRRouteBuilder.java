package com.vpn.integration.route;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.component.file.FileComponent;

import com.vpn.integration.route.rfq.aggregation.RFQResponseAggregationStrategy;
import com.vpn.integration.route.rfq.beanprocessor.DramaProcessor;
import com.vpn.integration.route.rfq.beanprocessor.FictionProcessor;
import com.vpn.integration.route.rfq.beanprocessor.FinalOutputProcessor;

public class JMSToSplitterToCBRRouteBuilder extends RouteBuilder {

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
		
		from("jms:rfq")
				.id("queue:rfq->split->cbr->queue:[DRAMA|FICTION]")
				.transacted("required")
				.split(xPathBuilder, new RFQResponseAggregationStrategy()).parallelProcessing().timeout(10000)
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
