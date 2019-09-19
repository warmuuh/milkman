#!/usr/bin/env sh

BASE_DIR="$(cd "$(dirname "$0")"; pwd)" || exit 2

JRE_DIR="$BASE_DIR"/jre-linux64
if [[ "$OSTYPE" == "darwin"* ]]; then
	JRE_DIR="$BASE_DIR"/jre-macos64
fi

chmod +x "$JRE_DIR"/bin/java

"$JRE_DIR"/bin/java \
	-client \
	-cp "$BASE_DIR"/plugins/*:"$BASE_DIR"/milkman.jar:"$BASE_DIR"/milkman-cli.jar milkmancli.MilkmanCli $@
	