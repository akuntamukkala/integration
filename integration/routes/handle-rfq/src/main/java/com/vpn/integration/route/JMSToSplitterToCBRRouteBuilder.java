package com.vpn.integration.route;

import org.apache.camel.Processor;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.spring.SpringRouteBuilder;

import com.vpn.integration.route.rfq.aggregation.RFQResponseAggregationStrategy;
import com.vpn.integration.route.rfq.beanprocessor.RedeliveryDelayProcessor;
import com.vpn.integration.route.rfq.exception.RFQRuntimeException;

public class JMSToSplitterToCBRRouteBuilder extends SpringRouteBuilder {

	private String outputFileDirectory;
	private Processor dramaCategoryPriceCalculator;
	private Processor fictionCategoryPriceCalculator;
	

	@Override
	public void configure() throws Exception {

		XPathBuilder xPathBuilder = new XPathBuilder("//rfq/books/item"); 
		
		onException(RFQRuntimeException.class).handled(true).to("jms:unknown-runtime-exception");
		
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
			.when(header("itemType").isEqualTo("FICTION")).convertBodyTo(String.class).process(this.fictionCategoryPriceCalculator)
			.when(header("itemType").isEqualTo("DRAMA")).convertBodyTo(String.class).process(this.dramaCategoryPriceCalculator)
			.otherwise().to("jms:UnknownType");
		
	}

	public String getOutputFileDirectory() {
		return outputFileDirectory;
	}

	public void setOutputFileDirectory(String outputFileDirectory) {
		this.outputFileDirectory = outputFileDirectory;
	}
	
	
	public Processor getDramaCategoryPriceCalculator() {
		return dramaCategoryPriceCalculator;
	}

	public void setDramaCategoryPriceCalculator(
			Processor dramaCategoryPriceCalculator) {
		this.dramaCategoryPriceCalculator = dramaCategoryPriceCalculator;
	}

	public Processor getFictionCategoryPriceCalculator() {
		return fictionCategoryPriceCalculator;
	}

	public void setFictionCategoryPriceCalculator(
			Processor fictionCategoryPriceCalculator) {
		this.fictionCategoryPriceCalculator = fictionCategoryPriceCalculator;
	}
}
