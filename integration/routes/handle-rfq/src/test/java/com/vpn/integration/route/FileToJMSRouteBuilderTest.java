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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import com.vpn.integration.route.FileToJMSRouteBuilder;

public class FileToJMSRouteBuilderTest extends CamelTestSupport {

	@EndpointInject(uri = "mock:received")
	MockEndpoint received;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
			"vm://test-broker?broker.persistent=false");
	@Before
	@Override
	public void setUp() throws Exception {
		FileUtils.copyFileToDirectory(new File("./src/test/resources/testRFQ.xml"), folder.getRoot().getAbsoluteFile());
		super.setUp();
	}
	
	@Test
	public void testFile2JMSRoute() throws InterruptedException, JMSException, IOException {
		
        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();


        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue("rfq");

        // Create a MessageConsumer from the Session to the Topic or Queue
        MessageConsumer consumer = session.createConsumer(destination);

        // Wait for a message
        Message message = consumer.receive(2000);
        Assert.assertEquals("rfqId header on the JMS message not the same as //rfq/id", "123456", message.getStringProperty("rfqId"));
        Assert.assertNotNull(message);
        
        
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            Assert.assertEquals("The content from src/test/resources/testRFQ.xml should have the same as the content retrieved from the jms message", FileUtils.readFileToString(new File("./src/test/resources/testRFQ.xml"), "UTF-8"), text);
        } else {
        	fail("message should have been a TextMessage");
        }

        consumer.close();
        session.close();
        connection.close();		
	}

	
	@Override
	protected RouteBuilder[] createRouteBuilders() throws Exception {
		addTestJmsComponent();
		FileToJMSRouteBuilder routeBuilder = new FileToJMSRouteBuilder();
		routeBuilder.setIncomingFileDirectory(folder.getRoot().getAbsolutePath());
		return new RouteBuilder[] {  routeBuilder};
	}

	private void addTestJmsComponent() {
		
		JmsConfiguration jmsConfig = new JmsConfiguration(connectionFactory);
		JmsComponent component = new JmsComponent(jmsConfig);
		
		
		context.addComponent("jms", component);
		
	}
}
