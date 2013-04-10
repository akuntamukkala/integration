package com.vpn.integration.route;

import java.io.File;
import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Assert;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.xml.sax.SAXException;

public class JMSToSplitterToCBRRouteBuilderTest extends CamelTestSupport {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
			"vm://test-broker?broker.persistent=false");

	@Before
	@Override
	public void setUp() throws Exception {
		FileUtils.copyFileToDirectory(new File(
				"./src/test/resources/testRFQ.xml"), folder.getRoot()
				.getAbsoluteFile());
		super.setUp();
	}

	@Test
	public void testFile2JMSRoute() throws InterruptedException, JMSException,
			IOException, SAXException {

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		// Create a Session
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue)
		Destination destinationFiction = session.createQueue("FICTION");

		// Create a MessageConsumer from the Session to the Topic or Queue
		MessageConsumer consumerFiction = session
				.createConsumer(destinationFiction);

		// Wait for a message
		Message messageFiction = consumerFiction.receive(2000);

		if (messageFiction instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) messageFiction;
			String text = textMessage.getText();

			XMLUnit.setIgnoreWhitespace(true);
			Diff diff = new Diff(FileUtils.readFileToString(new File(
					"./src/test/resources/item-fiction.xml"), "UTF-8"), text);

			System.out.println(FileUtils.readFileToString(new File(
					"./src/test/resources/item-fiction.xml"), "UTF-8"));
			System.out.println(text);

			Assert.assertTrue(
					"The content from src/test/resources/item-fiction.xml should have the same as the content retrieved from the jms message",
					diff.similar());
		} else {
			fail("message should have been a TextMessage");
		}

		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue("DRAMA");

		// Create a MessageConsumer from the Session to the Topic or Queue
		MessageConsumer consumer = session.createConsumer(destination);

		// Wait for a message
		Message message = consumer.receive(2000);

		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			String text = textMessage.getText();
			XMLUnit.setIgnoreWhitespace(true);
			Diff diff = new Diff(FileUtils.readFileToString(new File(
					"./src/test/resources/item-drama.xml"), "UTF-8"), text);
			System.out.println(FileUtils.readFileToString(new File(
					"./src/test/resources/item-drama.xml"), "UTF-8"));
			System.out.println(text);
			Assert.assertTrue(
					"The content from src/test/resources/item-drama.xml should have the same as the content retrieved from the jms message",
					diff.similar());

		} else {
			fail("message should have been a TextMessage");
		}

		consumerFiction.close();
		consumer.close();
		session.close();
		connection.close();
	}

	@Override
	protected RouteBuilder[] createRouteBuilders() throws Exception {
		addTestJmsComponent();
		FileToJMSRouteBuilder file2JmsRouteBuilder = new FileToJMSRouteBuilder();
		file2JmsRouteBuilder.setIncomingFileDirectory(folder.getRoot()
				.getAbsolutePath());

		JMSToSplitterToCBRRouteBuilder jmsToSplitterToCBRRouteBuilder = new JMSToSplitterToCBRRouteBuilder();

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
		
		JmsComponent component = JmsComponent.jmsComponentTransacted(
				connectionFactory, transactionManager);

		context.addComponent("jms", component);

	}
}
