#!/bin/sh
cd ../../
cd cghr-services
gradle install
cd ..
cd hc/services
gradle  runJetty8
