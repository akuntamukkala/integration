package com.vpn.integration.route.rfq.vo;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;
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

	public static void main1(String[] args) {
		Item item1 = new Item();
		item1.setCost(new BigDecimal("20.00"));
		item1.setIsbn("2342342");
		item1.setQuantity(new Integer(20));
		item1.setType("DRAMA");
		
		
		Item item2 = new Item();
		item2.setCost(new BigDecimal("21.00"));
		item2.setIsbn("ereerter");
		item2.setQuantity(new Integer(10));
		item2.setType("FICTION");
		
		List<Item> items = new ArrayList<Item>();
		items.add(item1);
		items.add(item2);
		
		Books books = new Books();
		books.setItems(items);
		
		RFQ rfq = new RFQ("123456", "1.0.0", books);
		
		XStream xstream = new XStream();
		xstream.processAnnotations(RFQ.class);
		System.out.println(xstream.toXML(rfq));
		
		
	}
	
	public static void main(String[] args) {
		XStream xstream = new XStream();
		xstream.processAnnotations(RFQ.class);

		RFQ rfq = (RFQ) xstream.fromXML(new File("./src/test/resources/testRFQ.xml"));
		System.out.println("hello");
	}
	
}
