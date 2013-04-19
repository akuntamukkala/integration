package com.vpn.integration.route.rfq.vo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("rfq")
public class RFQ {

	private String id;
	
	@XStreamAsAttribute
	private String version;
	
	
	private Books books;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Books getBooks() {
		return books;
	}

	public void setBooks(Books books) {
		this.books = books;
	}

	public RFQ(String id, String version, Books books) {
		this.id = id;
		this.version = version;
		this.books = books;
	}
	
}
