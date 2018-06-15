#!/usr/bin/env bash

#iperf -s

java -jar /home/viscous/Desktop/SeparateServer.jar /home/viscous/Documents/f6/ 2>&1 | tee /tmp/ser.log
