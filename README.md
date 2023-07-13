# ES_Buffer_Project

## Internship project for Saptarshi De Chaudhury, Using Buffer to index data into ElasticSearch


#### Used scheduling in Spring to schedule Producer and Consumer, producing batches of documents to the buffer and consuming them and indexing into ElasticSearch.

#### Generated Performance graphs within the code. The performance of Kafka and MongoDB can be compared.

#### Used single as well as multiple consumers to analyse the scalability of the system.


##### To run the code, need to pass arguments for name of Buffer to be used. The argument will be injected into the application.properties file.

--buffer.type=mongodb 

or

--buffer.type=kafka
