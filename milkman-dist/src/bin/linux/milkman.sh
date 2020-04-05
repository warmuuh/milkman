#!/usr/bin/env sh

BASE_DIR="$(cd "$(dirname "$0")"; pwd)" || exit 2

chmod +x "$BASE_DIR"/jre-linux64/bin/java

"$BASE_DIR"/jre-linux64/bin/java -XX:SharedArchiveFile="$BASE_DIR"/app-cds.jsa \
	-client \
	-XX:+UseCompressedOops \
	-XX:+UseCompressedClassPointers \
	--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
  --add-exports javafx.controls/com.sun.javafx.scene.control.skin=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED \
	--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED \
	--add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
	-Dprism.dirtyopts=false -cp "$BASE_DIR"/plugins/*:"$BASE_DIR"/milkman.jar milkman.MilkmanApplication
