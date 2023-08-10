# eth-jmeter
Conducting stress testing on the Ethereum-compatible chain's RPC interface using JMeter.
## require
java8,maven,linux or mac(not support win)

## use
1. modify jmx
```shell
mvn clean package
mvn jmeter:gui
# "Load and modify the JMX files in the src/test/jmeter directory.
```
2. start stress testing
```shell
mvn jmeter:jmeter
```