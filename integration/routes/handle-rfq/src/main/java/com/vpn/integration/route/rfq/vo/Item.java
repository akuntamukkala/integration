package com.vpn.integration.route.rfq.vo;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("item")
public class Item {
	private String isbn;
	private Integer quantity;
	
	@XStreamAsAttribute
	private String type;
	private BigDecimal cost;
	
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public static void main(String[] args) {
		Item item = new Item();
		item.setIsbn("werwerwr");
		item.setQuantity(new Integer(10));
		item.setType("DRAMA");
		BigDecimal bd = new BigDecimal("100.34");
		item.setCost(bd);
		XStream xstream = new XStream();
		xstream.processAnnotations(Item.class);
//		xstream.useAttributeFor("type", String.class);
//		xstream.alias("item", Item.class);
		System.out.println(xstream.toXML(item));
	}
	public BigDecimal getCost() {
		return cost;
	}
	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}
	
}
