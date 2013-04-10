package com.vpn.integration.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;

public class JMSToSplitterToCBRRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		from("jms:rfq").id("queue:rfq->split->cbr->queue:[DRAMA|FICTION]").transacted("required").split().tokenizeXML("item", "books").streaming().
			setHeader("itemType", XPathBuilder.xpath("//item/@type", String.class)).
				choice()
					.when(header("itemType").isEqualTo("FICTION")).to("jms:FICTION")
					.when(header("itemType").isEqualTo("DRAMA")).to("jms:DRAMA")
					.otherwise().to("jms:UnknownType");
	}
}
