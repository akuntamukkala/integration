integration
===========

The objective of this project is to help create a reference implementation for Karaf feature containing common configuration for a few message routes implemented using Camel, Blueprint (for wiring the configuration/bundles) and ActiveMQ Broker.

Karaf runtime enhanced with Camel/Blueprint/ActiveMQ runs the routes once the developed feature is deployed. 

The usecase is as follows:

1. Retrieve request for quote for certain categories of books.
2. Tiered pricing based on volume purchase
3. The aggregated response contains the price per category of books

Sample Request:

&lt;rfq version=&quot;1.0.0&quot;&gt;
  &lt;id&gt;123456&lt;/id&gt;
	&lt;books&gt;
		&lt;item type=&quot;FICTION&quot;&gt;
			&lt;isbn&gt;0486284727&lt;/isbn&gt;
			&lt;quantity&gt;10&lt;/quantity&gt;
		&lt;/item&gt;
		&lt;item type=&quot;DRAMA&quot;&gt;
			&lt;isbn&gt;0486272788&lt;/isbn&gt;
			&lt;quantity&gt;5&lt;/quantity&gt;
		&lt;/item&gt;
	&lt;/books&gt;
&lt;/rfq&gt;


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
