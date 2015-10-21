# spark-streaming
spark-streaming sample project

    Dependencies:
        Java 8
        Scala 2.10.4
        SBT 0.13.X
        IDE (with Scala/SBT plugin installed - IntelliJ/Eclipse)
        Kafka 0.8.1

    How to run KafkaUAP locally:
        Once the project is setup in the IDE, just check if the mentioned versions(Java 8, Scala 2.10.4, SBT 0.13.X) are project defaults
        
        Run kafka
            In a new console tab (Zookeeper) 
                % cd <KAFKA_HOME> 
                % nohup ./bin/zookeeper-server-start.sh ./config/zookeeper.properties > /dev/null 2>&1 &
            In a new console tab (Broker)
                % cd <KAFKA_HOME>
                % nohup env JMX_PORT=9991 ./bin/kafka-server-start.sh ./config/server.properties > /dev/null 2>&1
                % ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic uap-input
                % ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic uap-output
            In a new console tab (Producer)
                % cd <KAFKA_HOME>
                % bin/kafka-console-producer.sh --broker-list localhost:9092 --topic uap-input
            In a new console tab (Consumer)
                % cd <KAFKA_HOME>
                % bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning
        
        Run Streaming Job in IDE
            Right click on the object KafkaUAP and you should not see any error in the console output, this job must be started after
            starting kafka
             
        Testing
            In the Producer tab publish a User Agent String
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; fr; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5,gzip(gfe),gzip(gfe)"
            
            In the Consumer tab you would see the processed result in less than 5 seconds, if you don't see it, debugging required
    
    Note:
        For convenience reasons hostname and port name are hard-coded (Zookeeper localhost:2181, KafkaBroker: localhost:9092)
        These are default values, for some reason if it is different for you please update in code and you are good to go.