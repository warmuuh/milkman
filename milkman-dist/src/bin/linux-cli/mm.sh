#!/usr/bin/env sh

BASE_DIR="$(cd "$(dirname "$0")"; pwd)" || exit 2

chmod +x "$BASE_DIR"/jre-linux64/bin/java

"$BASE_DIR"/jre-linux64/bin/java \
	-client \
	-cp "$BASE_DIR"/plugins/*:"$BASE_DIR"/milkman.jar:"$BASE_DIR"/milkman-cli.jar milkmancli.MilkmanCli $@
	