package com.vpn.integration.route;

import java.io.File;
import java.io.IOException;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.RedeliveryPolicyMap;
import org.apache.activemq.broker.util.RedeliveryPlugin;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.xml.sax.SAXException;

import com.vpn.integration.route.rfq.beanprocessor.DramaCategoryPriceCalculator;
import com.vpn.integration.route.rfq.beanprocessor.FictionCategoryPriceCalculator;

public class JMSToSplitterToCBRRouteBuilderTest extends CamelTestSupport {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File incoming;
	private File outgoing;

	private static ConnectionFactory connectionFactory = null;

	private BrokerService broker = null;

	private static Logger log = LoggerFactory
			.getLogger(JMSToSplitterToCBRRouteBuilderTest.class);

	@Before
	@Override
	public void setUp() throws Exception {

		broker = new BrokerService();
		broker.setBrokerName("test-broker");
		broker.setPersistent(false);
		broker.setUseJmx(false);
		broker.setSchedulerSupport(true);

		RedeliveryPlugin redeliveryPlugin = new RedeliveryPlugin();
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setInitialRedeliveryDelay(2000);
		redeliveryPolicy.setRedeliveryDelay(10000);
		redeliveryPolicy.setUseExponentialBackOff(false);
		redeliveryPolicy.setMaximumRedeliveries(-1);
		redeliveryPolicy.setQueue("*");
		RedeliveryPolicyMap redeliveryPolicyMap = new RedeliveryPolicyMap();
		redeliveryPolicyMap.put(new ActiveMQQueue("rfq"), redeliveryPolicy);
		redeliveryPlugin.setRedeliveryPolicyMap(redeliveryPolicyMap);
		redeliveryPlugin.installPlugin(broker.getBroker());
		broker.start();

		incoming = folder.newFolder("Incoming");
		outgoing = folder.newFolder("Outgoing");

		FileUtils
				.copyFileToDirectory(new File(
						"./src/test/resources/testRFQ.xml"), incoming
						.getAbsoluteFile());

		connectionFactory = new ActiveMQConnectionFactory(
				"vm://test-broker?create=false&broker.persistent=false");

		RedeliveryPolicy cfRedeliveryPolicy = new RedeliveryPolicy();
		cfRedeliveryPolicy.setInitialRedeliveryDelay(0);
		cfRedeliveryPolicy.setRedeliveryDelay(10000);
		cfRedeliveryPolicy.setUseExponentialBackOff(false);
		cfRedeliveryPolicy.setMaximumRedeliveries(-1);

		((ActiveMQConnectionFactory) connectionFactory)
				.getRedeliveryPolicyMap().put(new ActiveMQQueue(">"),
						cfRedeliveryPolicy);

		super.setUp();

	}

	@Test
	public void test() throws InterruptedException, IOException, SAXException {
		Thread.sleep(3000);
		String expected = FileUtils.readFileToString(new File(
				"./src/test/resources/123456-output.xml"), "UTF-8");

		String actual = FileUtils.readFileToString(
				new File(outgoing.getAbsolutePath() + File.separator
						+ "123456-output.xml"), "UTF-8");
		XMLUnit.setIgnoreWhitespace(true);
		Diff diff = new Diff(expected, actual);
		if (!diff.identical()) {
			log.error("The expected RFQ does not match actual. Listing the differences...");
			DetailedDiff detailedDiff = new DetailedDiff(diff);
			for (Object difference : detailedDiff.getAllDifferences()) {
				log.info(((Difference) difference).getDescription());
			}
			fail("The expected resultant RFQ does not match actual");
		}
	}

	@Override
	protected RouteBuilder[] createRouteBuilders() throws Exception {
		addTestJmsComponent();

		FileToJMSRouteBuilder file2JmsRouteBuilder = new FileToJMSRouteBuilder();
		file2JmsRouteBuilder.setIncomingFileDirectory(incoming
				.getAbsolutePath());

		JMSToSplitterToCBRRouteBuilder jmsToSplitterToCBRRouteBuilder = new JMSToSplitterToCBRRouteBuilder();
		jmsToSplitterToCBRRouteBuilder.setOutputFileDirectory(outgoing
				.getAbsolutePath());
		jmsToSplitterToCBRRouteBuilder
				.setDramaCategoryPriceCalculator(new DramaCategoryPriceCalculator());
		jmsToSplitterToCBRRouteBuilder
				.setFictionCategoryPriceCalculator(new FictionCategoryPriceCalculator());

		return new RouteBuilder[] { file2JmsRouteBuilder,
				jmsToSplitterToCBRRouteBuilder };
	}

	@Override
	protected JndiRegistry createRegistry() throws Exception {
		JndiRegistry reg = super.createRegistry();

		org.springframework.jms.connection.JmsTransactionManager transactionManager = new org.springframework.jms.connection.JmsTransactionManager();
		transactionManager.setConnectionFactory(connectionFactory);
		reg.bind("txManager", transactionManager);

		SpringTransactionPolicy txPolicy = new SpringTransactionPolicy();
		txPolicy.setTransactionManager(transactionManager);
		txPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
		reg.bind("required", txPolicy);

		return reg;
	}

	private void addTestJmsComponent() {

		org.springframework.jms.connection.JmsTransactionManager transactionManager = new org.springframework.jms.connection.JmsTransactionManager();
		transactionManager.setConnectionFactory(connectionFactory);

		ActiveMQComponent c = new ActiveMQComponent();
		c.setConnectionFactory(connectionFactory);
		c.setTransacted(true);

		c.setTransactionManager((JmsTransactionManager) context.getRegistry()
				.lookup("txManager"));
		context.addComponent("jms", c);

	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		broker.stop();
	}

}
