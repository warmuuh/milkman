#!/bin/sh
chmod +x jre-macos64/bin/java

./jre-macos64/bin/java \
	-client \
	-cp plugins/*:milkman.jar:milkman-cli.jar milkmancli.MilkmanCli $@
