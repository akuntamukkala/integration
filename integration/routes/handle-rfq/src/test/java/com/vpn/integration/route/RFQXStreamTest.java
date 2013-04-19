package com.vpn.integration.route;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.vpn.integration.route.rfq.vo.Books;
import com.vpn.integration.route.rfq.vo.Item;
import com.vpn.integration.route.rfq.vo.RFQ;

public class RFQXStreamTest {
	private static Logger log = LoggerFactory
			.getLogger(RFQXStreamTest.class);

	@Test
	public void testXmlToJava() {
		
		XStream xstream = new XStream();
		xstream.processAnnotations(RFQ.class);

		RFQ rfq = (RFQ) xstream.fromXML(new File("./src/test/resources/testRFQ.xml"));
		assertEquals("123456", rfq.getId());
		assertEquals("1.0.0", rfq.getVersion());
		assertNotNull(rfq.getBooks());
		assertNotNull(rfq.getBooks().getItems());
		assertEquals(2, rfq.getBooks().getItems().size());
		Collections.sort(rfq.getBooks().getItems(), new Comparator<Item>() {

			@Override
			public int compare(Item o1, Item o2) {
				return o1.getIsbn().compareTo(o2.getIsbn());
			}
		});
		
		Item first = rfq.getBooks().getItems().get(0);
		assertEquals("0486272788", first.getIsbn());
		assertEquals(new Integer(5), first.getQuantity());
		assertEquals("DRAMA", first.getType());
		
		Item second = rfq.getBooks().getItems().get(1);
		assertEquals("0486284727", second.getIsbn());
		assertEquals(new Integer(10), second.getQuantity());
		assertEquals("FICTION", second.getType());
		
	}
	
	@Test
	public void testJavaToXML() throws Exception {
		
		Item item1 = new Item();
		item1.setCost(new BigDecimal("25.54"));
		item1.setIsbn("0486284727");
		item1.setQuantity(new Integer(10));
		item1.setType("FICTION");
		
		
		Item item2 = new Item();
		item2.setCost(new BigDecimal("5.84"));
		item2.setIsbn("0486272788");
		item2.setQuantity(new Integer(5));
		item2.setType("DRAMA");
		
		List<Item> items = new ArrayList<Item>();
		items.add(item1);
		items.add(item2);
		
		Books books = new Books();
		books.setItems(items);
		
		RFQ rfq = new RFQ("123456", "1.0.0", books);
		
		XStream xstream = new XStream();
		xstream.processAnnotations(RFQ.class);
		
		String actual = xstream.toXML(rfq);
		
		String expected = FileUtils.readFileToString(new File(
				"./src/test/resources/123456-output.xml"), "UTF-8");

		XMLUnit.setIgnoreWhitespace(true);
		Diff diff = new Diff(expected, actual);
		
		if (!diff.similar()) {
			log.error("The expected marshalled XML does not match actual. Listing the differences...");
			DetailedDiff detailedDiff = new DetailedDiff(diff);
			for (Object difference : detailedDiff.getAllDifferences()) {
				log.info(((Difference) difference).getDescription());
			}
			fail("The expected marshalled XML does not match actual.");
		}
	}

}
