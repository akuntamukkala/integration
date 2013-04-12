package com.vpn.integration.route.rfq.vo;

import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("books")
public class Books {
	
	@XStreamImplicit(itemFieldName="item")
	private List<Item> items;
	
	
	public List<Item> getItems() {
		return items;
	}


	public void setItems(List<Item> items) {
		this.items = items;
	}


	public Books(Item... items) {
		this.items = Arrays.asList(items);
	}
}
