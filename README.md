integration
===========

The objective of this project is to help create a reference implementation for Karaf feature containing common configuration for a few message routes implemented using Camel, Blueprint (for wiring the configuration/bundles) and ActiveMQ Broker.

Karaf runtime enhanced with Camel/Blueprint/ActiveMQ runs the routes once the developed feature is deployed. 

The usecase is as follows:

1. Retrieve request for quote for certain categories of books.
2. Tiered pricing based on volume purchase
3. The aggregated response contains the price per category of books

Sample Request:
<rfq version="1.0.0">
  <id>123456</id>
	<books>
		<item type="FICTION">
			<isbn>0486284727</isbn>
			<quantity>10</quantity>
		</item>
		<item type="DRAMA">
			<isbn>0486272788</isbn>
			<quantity>5</quantity>
		</item>
	</books>
</rfq>

Expected Response:
<rfq version="1.0.0">
  <id>123456</id>
  <books>
    <item type="FICTION">
      <isbn>0486284727</isbn>
      <quantity>10</quantity>
      <cost>25.54</cost>
    </item>
    <item type="DRAMA">
      <isbn>0486272788</isbn>
      <quantity>5</quantity>
      <cost>5.84</cost>
    </item>
  </books>
</rfq>

The project uses split, content based routing, aggregation EIPs.

This is a multi-module Maven project. 

To Do:

Write Usage instructions :) 
