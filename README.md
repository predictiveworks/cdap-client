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

# Use Cases

This CDAP-Client has been built with the intent to provide a relevant building block for an [Elasticsearch Plugin](https://github.com/skrusche63/elastic-insight) to connect a broad range data analytic & machine learning platform with an outstanding search engine.

## Basic Threat Hunting

Threat Hunting e.g. based on Windows Event Logs (and of course other data sources) can be implemented by developing analytic applications in CDAP. As an alternative, many Cyber Defense applications are predefined in [Predictive Works](https://predictiveworks.eu). 

The [Elasticsearch Plugin](https://github.com/skrusche63/elastic-insight) connects these applications to the ELK stack. Applications can be started or stopped, monitored and results can be explored, fully under control from within Elasticsearch. 

![alt Basic Threat Hunting with CDAP](https://github.com/skrusche63/cdap-client/blob/master/images/threat-hunting.svg)

Exploring suspicious activities in Windows Event Logs is just an example. One may think of other data sources or other use cases such as DGA botnet prediction or malware analysis. The technical setup is always the same and the [Elasticsearch Plugin](https://github.com/skrusche63/elastic-insight) is the missing link to open the door into a new world.
