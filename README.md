# CDAP-Client

A lightweight Java client for [CDAP](https://cdap.io). 

CDAP is an open source framework for building data analytic applications. It addresses a broad range of real-time and batch use cases and accelerates big data application development.

This Java client is based on the original [CDAP-Client](https://github.com/cdapio/cdap/tree/v5.1.1/cdap-client), but removes nested and unneeded dependencies. Furthermore, deserialization issues concerning CDAP Http responses were addressed.   

Contributions refer to the following REST APIs:

* Application
* Dataset
* Metrics
* Monitor
* Program
* Query

# Use Case

This CDAP-Client has been built with the intent to provide a relevant building block for an [Elasticsearch Plugin](https://github.com/skrusche63/elastic-insight) to connect a broad range data analytic & machine learning platform with an outstanding search engine.
