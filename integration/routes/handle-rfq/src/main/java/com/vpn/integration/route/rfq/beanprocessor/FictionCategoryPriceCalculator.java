package com.vpn.integration.route.rfq.beanprocessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.thoughtworks.xstream.XStream;
import com.vpn.integration.route.rfq.vo.Item;

public class FictionCategoryPriceCalculator implements Processor {

	private static XStream xstream = new XStream();
	private static Map<String, BigDecimal> isbnPriceMap = new HashMap<String, BigDecimal>();
	private static BigDecimal HUNDRED = new BigDecimal("100");

	private static BigDecimal DISCOUNT_TIER1 = new BigDecimal("2.00");
	private static BigDecimal DISCOUNT_TIER2 = new BigDecimal("5.00");
	private static BigDecimal DISCOUNT_TIER3 = new BigDecimal("10.00");

	static {
		xstream.useAttributeFor("type", String.class);
		xstream.alias("item", Item.class);

		isbnPriceMap.put("0486295060", new BigDecimal("7.11"));
		isbnPriceMap.put("0486284727", new BigDecimal("2.70"));
		isbnPriceMap.put("0486270718", new BigDecimal("2.25"));

	}

	private static BigDecimal getDiscountedCost(Item item, BigDecimal discountTier) {
		return isbnPriceMap.get(item.getIsbn()).multiply(
				new BigDecimal(item.getQuantity())
						.subtract(isbnPriceMap.get(item.getIsbn())
								.multiply(
										new BigDecimal(item
												.getQuantity())
												.multiply(
														discountTier))
												.divide(HUNDRED, RoundingMode.HALF_EVEN))).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public static void main(String[] args) {
		Item item = new Item();
		item.setIsbn("0486284727");
		item.setQuantity(new Integer(100));
		item.setType("DRAMA");
		//BigDecimal bd = new BigDecimal("100.34");
//		bd.setScale(2, RoundingMode.HALF_EVEN);
	//	System.out.println(bd);
		//item.setCost(bd);
		XStream xstream = new XStream();
		xstream.useAttributeFor("type", String.class);
		xstream.alias("item", Item.class);
		System.out.println(xstream.toXML(item));
		
		item.setCost(getDiscountedCost(item, DISCOUNT_TIER1));
		System.out.println(xstream.toXML(item));
		
		
		
	}
	@Override
	public void process(Exchange exchange) throws Exception {

		//Thread.sleep(30000);
		
		String itemStr = (String) exchange.getIn().getBody();
		Item item = (Item) xstream.fromXML(itemStr);
		if (isbnPriceMap.get(item.getIsbn()) != null) {
			if (item.getQuantity() > 0 && item.getQuantity() <= 10) {
				item.setCost(getDiscountedCost(item, DISCOUNT_TIER1));

			} else if (item.getQuantity() > 10 && item.getQuantity() <= 50) {
				item.setCost(getDiscountedCost(item, DISCOUNT_TIER2));

			} else if (item.getQuantity() > 50) {
				item.setCost(getDiscountedCost(item, DISCOUNT_TIER3));
			} 
		}
		
		XStream xstream = new XStream();
		xstream.useAttributeFor("type", String.class);
		xstream.alias("item", Item.class);
		exchange.getIn().setBody(xstream.toXML(item));

	}


}
