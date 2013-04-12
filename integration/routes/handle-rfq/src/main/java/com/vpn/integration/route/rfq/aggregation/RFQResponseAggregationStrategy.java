package com.vpn.integration.route.rfq.aggregation;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.thoughtworks.xstream.XStream;
import com.vpn.integration.route.rfq.vo.Books;
import com.vpn.integration.route.rfq.vo.Item;
import com.vpn.integration.route.rfq.vo.RFQ;

public class RFQResponseAggregationStrategy implements AggregationStrategy {

	
	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

		if (oldExchange == null) {
			
			List<Item> items = new ArrayList<Item>();
			XStream xstream = new XStream();
			xstream.processAnnotations(Item.class);
			Item item = (Item) xstream.fromXML(newExchange.getIn().getBody(String.class));
			items.add(item);
			Books books = new Books();
			books.setItems(items);
			RFQ rfq = new RFQ((String)newExchange.getIn().getHeader("rfqId"), (String) newExchange.getIn().getHeader("version"), books);
			xstream.processAnnotations(RFQ.class);
			newExchange.getIn().setBody(xstream.toXML(rfq));
			return newExchange;
			
		} else {

			Message newIn = newExchange.getIn();
			String rfqStr = oldExchange.getIn().getBody(String.class);
			XStream xstream = new XStream();
			xstream.processAnnotations(RFQ.class);
			RFQ rfq = (RFQ)xstream.fromXML(rfqStr);
			
			String itemStr = newIn.getBody(String.class);
			xstream.processAnnotations(Item.class);
			Item item = (Item) xstream.fromXML(itemStr);
			
			rfq.getBooks().getItems().add(item);
			xstream.processAnnotations(RFQ.class);
			newExchange.getIn().setBody(xstream.toXML(rfq));
			return newExchange;
		}

	}

}
