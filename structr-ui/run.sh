#!/bin/bash
java -Dfile.encoding=utf-8 -Xms1g -Xmx1g -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseNUMA -cp target/lib/*:target/structr-ui-0.8.2.jar org.structr.Ui
