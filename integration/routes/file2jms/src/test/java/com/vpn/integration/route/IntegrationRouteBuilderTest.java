package com.vpn.integration.route;

import java.io.File;

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


public class IntegrationRouteBuilderTest extends CamelTestSupport {

	@EndpointInject(uri = "mock:received")
	MockEndpoint received;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(//"tcp://localhost:61636");
			"vm://test-broker?broker.persistent=false");
	@Before
	@Override
	public void setUp() throws Exception {
		FileUtils.copyFileToDirectory(new File("./src/test/resources/testRFQ.xml"), folder.getRoot().getAbsoluteFile());
		super.setUp();
	}
	
	@Test
	public void testFile2JMSRoute() throws InterruptedException, JMSException {
		
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

        Assert.assertNotNull(message);
        
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("Received: " + text);
        } else {
            System.out.println("Received: " + message);
        }

        consumer.close();
        session.close();
        connection.close();		
	}

	public static void main(String[] args) throws Exception {
        try {

            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61636");

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
            Message message = consumer.receive(1000);

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                System.out.println("Received: " + text);
            } else {
                System.out.println("Received: " + message);
            }

            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

	
	@Override
	protected RouteBuilder[] createRouteBuilders() throws Exception {
		addTestJmsComponent();
		IntegrationRouteBuilder routeBuilder = new IntegrationRouteBuilder();
		routeBuilder.setIncomingFileDirectory(folder.getRoot().getAbsolutePath());
		return new RouteBuilder[] {  routeBuilder};
	}

	private void addTestJmsComponent() {
		
		JmsConfiguration jmsConfig = new JmsConfiguration(connectionFactory);
		JmsComponent component = new JmsComponent(jmsConfig);
		
		
		context.addComponent("jms", component);
		
	}
}
