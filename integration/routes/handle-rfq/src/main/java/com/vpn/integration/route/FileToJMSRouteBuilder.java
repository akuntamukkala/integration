package com.vpn.integration.route;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.xml.sax.SAXParseException;

public class FileToJMSRouteBuilder extends RouteBuilder {

	private String incomingFileDirectory;
	private String malformedIncomingFileDirectory;
	
	public void setIncomingFileDirectory(String path) {
		this.incomingFileDirectory = path;
	}
	
	
	@Override
	public void configure() throws Exception {
		
		from("file://" + this.incomingFileDirectory).id("file->queue:rfq")
			.doTry()
				.to("validator:rfq.xsd?useDom=true")
				.to("direct:validinput")
			.doCatch(ValidationException.class)
				.log("Malformed XML input")
				.to("file://" + this.malformedIncomingFileDirectory);
				
		from("direct:validinput").setHeader("rfqId", XPathBuilder.xpath("//rfq/id/text()", String.class)).setHeader("version", XPathBuilder.xpath("//rfq/@version", String.class)).convertBodyTo(String.class, Charset.forName("UTF-8").name()).to("log:test").to("jms:rfq");
	}


	public String getMalformedIncomingFileDirectory() {
		return malformedIncomingFileDirectory;
	}


	public void setMalformedIncomingFileDirectory(
			String malformedIncomingFileDirectory) {
		this.malformedIncomingFileDirectory = malformedIncomingFileDirectory;
	}


}
