package com.vpn.integration.route;

import java.nio.charset.Charset;

import org.apache.camel.builder.RouteBuilder;

public class IntegrationRouteBuilder extends RouteBuilder {

	private String incomingFileDirectory;
	
	public void setIncomingFileDirectory(String path) {
		this.incomingFileDirectory = path;
	}
	@Override
	public void configure() throws Exception {
		
        from("file://" + this.incomingFileDirectory).id("file2jms").convertBodyTo(String.class, Charset.forName("UTF-8").name()).to("log:test")
        .to("jms:rfq");
		
        
	}

}
