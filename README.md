integration
===========

This project is work in progress. 

The objective of this project is to help create a reference implementation for Karaf feature containing common configuration for a few message routes implemented using Camel, Blueprint, CXF and ActiveMQ Broker.

Karaf runtime enhanced with Camel/Blueprint and CXF runs the routes once this feature is deployed. This project is not using an embedded ActiveMQ in Karaf. The Karaf feature being developed in project provides a bundle that exports an ActiveMQ connection factory (configurable endpoint) which gets imported by the other routes that need to produce/consume messages off ActiveMQ



