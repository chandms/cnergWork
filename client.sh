#!/usr/bin/env bash

java -jar /home/viscous/Desktop/cnerg.jar $1 $2 2>&1 | tee /tmp/log
