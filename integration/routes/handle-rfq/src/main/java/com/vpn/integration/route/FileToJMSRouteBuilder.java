package com.vpn.integration.route;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;

public class FileToJMSRouteBuilder extends RouteBuilder {

	private String incomingFileDirectory;
		
	public void setIncomingFileDirectory(String path) {
		this.incomingFileDirectory = path;
	}
	
	
	@Override
	public void configure() throws Exception {

		from("file://" + this.incomingFileDirectory).id("file->queue:rfq").setHeader("rfqId", XPathBuilder.xpath("//rfq/id/text()", String.class)).setHeader("version", XPathBuilder.xpath("//rfq/@version", String.class)).convertBodyTo(String.class, Charset.forName("UTF-8").name()).to("log:test")
        .to("jms:rfq");
	}

}
