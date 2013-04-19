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
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.junit.After;
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

	private File incoming;
	private File malformedInputFolder;

	private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61636");
//			"vm://test-broker?create=false&broker.persistent=false");
	
	private BrokerService broker = null;
	
	@Before
	@Override
	public void setUp() throws Exception {
		
		broker = new BrokerService();
		broker.setBrokerName("test-broker");
		broker.setPersistent(false);
		broker.setUseJmx(false);
		broker.start();
		
		incoming = folder.newFolder("Incoming");
		malformedInputFolder = folder.newFolder("MalformedInput");
		super.setUp();

		
	}
	
	
	
	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		broker.stop();
	}



	@Test
	public void testFile2JMSRoute() throws InterruptedException, JMSException, IOException {
		

		FileUtils.copyFileToDirectory(new File(
				"./src/test/resources/testRFQ.xml"), incoming.getAbsoluteFile());
		Thread.sleep(2000);
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

	
	@Test
	public void testMalformedXMLInput() throws Exception {	
		FileUtils.copyFileToDirectory(new File(
				"./src/test/resources/malformedRFQ.xml"), incoming.getAbsoluteFile());
		Thread.sleep(2000);	
		String actual = FileUtils.readFileToString(new File(malformedInputFolder.getAbsolutePath() + File.separator + "malformedRFQ.xml"), "UTF-8");
		Assert.assertNotNull("Input file containing malformed xml not found in malformed input folder", actual);
		Assert.assertEquals(FileUtils.readFileToString(new File("./src/test/resources/malformedRFQ.xml"), "utf-8"), actual);	
	}
	
	
	@Override
	protected RouteBuilder[] createRouteBuilders() throws Exception {
		addTestJmsComponent();
		FileToJMSRouteBuilder routeBuilder = new FileToJMSRouteBuilder();
		routeBuilder.setIncomingFileDirectory(incoming.getAbsolutePath());
		routeBuilder.setMalformedIncomingFileDirectory(malformedInputFolder.getAbsolutePath());
		
		return new RouteBuilder[] {  routeBuilder};
	}

	private void addTestJmsComponent() {
		
		JmsConfiguration jmsConfig = new JmsConfiguration(connectionFactory);
		JmsComponent component = new JmsComponent(jmsConfig);
		context.addComponent("jms", component);
		
	}
}
