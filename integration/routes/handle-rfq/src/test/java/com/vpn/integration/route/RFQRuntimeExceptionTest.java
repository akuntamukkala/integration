package com.vpn.integration.route;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.RedeliveryPolicyMap;
import org.apache.activemq.broker.util.RedeliveryPlugin;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.xml.sax.SAXException;

import com.vpn.integration.route.rfq.beanprocessor.DramaCategoryPriceCalculator;
import com.vpn.integration.route.rfq.exception.RFQRuntimeException;

public class RFQRuntimeExceptionTest extends CamelTestSupport {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File incoming;
	private File outgoing;

	private static ConnectionFactory connectionFactory = null;

	private BrokerService broker = null;

	public static void initialize() {

	}

	private static Logger log = LoggerFactory
			.getLogger(RFQRuntimeExceptionTest.class);

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
				"tcp://localhost:61636");
		// "vm://test-broker?create=false&broker.persistent=false");

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
	public void test() throws InterruptedException, IOException, SAXException,
			JMSException {
		Thread.sleep(3000);
		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		// Create a Session
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);

		
		try {
			// Create the destination (Topic or Queue)
			Queue unknownRuntimeExceptionDestination = session
					.createQueue("unknown-runtime-exception");

			QueueBrowser browser = session.createBrowser(unknownRuntimeExceptionDestination);
			Assert.assertTrue(browser.getEnumeration().hasMoreElements());
		} catch (Exception e) {
			fail("Exception occurred : " + e.getMessage());
		} finally {
			session.close();
			connection.close();
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

		Processor mockFictionProcessor = EasyMock
				.createNiceMock(Processor.class);
		mockFictionProcessor.process(isA(Exchange.class));
		expectLastCall().andThrow(new RFQRuntimeException());
		replay(mockFictionProcessor);

		jmsToSplitterToCBRRouteBuilder
				.setFictionCategoryPriceCalculator(mockFictionProcessor);

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
