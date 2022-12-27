# KVStore

## Compile

```
$ cd KVStore/
$ mvn install
```

## Deploy

### KVServer

To deploy a server run the following commands

```
$ cd KVStore/KVServer/target
$ java -cp KVServer-1.0-SNAPSHOT-jar-with-dependencies.jar org.notaris.KVServer -a IP -p port
```

You can deploy multiple servers.

### KVClient

To start a client run the following commands

```
$ cd KVStore/KVClient/target
java -cp KVClient-1.0-SNAPSHOT-jar-with-dependencies.jar org.notaris.KVClient -s /path/to/serverFile.txt -i /path/to/dataToIndex.txt -k replicationFactor
```

where

* serverFile.txt: contains all deployed servers and ports. For example
```
127.0.0.1 9995
127.0.0.1 9996
192.168.31.4 9995
192.168.31.4 9996
```

* dataToIndex.txt: contains data to index to servers. For example
```
person1 -> ["name" -> "John" | "age" -> 22 ]
person2 -> ["address" -> ["number" -> "12" | "street" -> "Panepistimiou"] | "name" -> "Mary"]
person3 -> ["height" -> 1.75 | "profession" -> "student"]
person4 -> []
```

* replication factor: the number of servers that every key will be inserted.
