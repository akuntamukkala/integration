package com.vpn.integration.route.rfq.beanprocessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.thoughtworks.xstream.XStream;
import com.vpn.integration.route.rfq.vo.Item;

public class DramaCategoryPriceCalculator implements Processor {

	private static XStream xstream = new XStream();
	private static Map<String, BigDecimal> isbnPriceMap = new HashMap<String, BigDecimal>();
	private static BigDecimal HUNDRED = new BigDecimal("100");

	private static BigDecimal DISCOUNT_TIER1 = new BigDecimal("10.00");
	private static BigDecimal DISCOUNT_TIER2 = new BigDecimal("20.00");
	private static BigDecimal DISCOUNT_TIER3 = new BigDecimal("50.00");

	static {
		xstream.useAttributeFor("type", String.class);
		xstream.alias("item", Item.class);

		isbnPriceMap.put("0486272788", new BigDecimal("1.35"));
		isbnPriceMap.put("0312144547", new BigDecimal("12.91"));
		isbnPriceMap.put("0312166214", new BigDecimal("12.67"));

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
	
	private static volatile int counter = 0;
	@Override
	public void process(Exchange exchange) throws Exception {
		
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
