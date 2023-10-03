# Kafka Tool

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=coverage)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool)

## Objectives

This is a tool for inspecting a running Kafka Cluster.

## Design 

A Kafka GUI Client. For the first version:

* Configure Brokers
* Topic listing
* Subscribe Topic
    * Formats:
        * AVRO
        * JSON
        * Plain Text
    * Clear messages
    * View Message
* Empty topic
* Windows Installer

## Development




```bash
./start-test-cluster.sh
mvn clean compile javafx:run
```


// https://app.moqups.com/ZOtwAtbxvpM52PyU8U4nRPsXeVoBukEi/view/page/a18c773d3