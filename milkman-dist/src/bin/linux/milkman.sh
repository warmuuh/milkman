#!/usr/bin/env sh

BASE_DIR="$(cd "$(dirname "$0")"; pwd)" || exit 2

chmod +x "$BASE_DIR"/jre-linux64/bin/java

# generate static cds
if [ ! -f "$BASE_DIR/jre-linux64/lib/client/classes.jsa" ]; then
    "$BASE_DIR/jre-linux64/bin/java" -Xshare:dump
fi

CDS_COMMAND=-XX:SharedArchiveFile=$BASE_DIR/app-cds.jsa
if [ ! -f "$BASE_DIR/app-cds.jsa" ]; then
  CDS_COMMAND=-XX:ArchiveClassesAtExit=$BASE_DIR/app-cds.jsa
fi


"$BASE_DIR"/jre-linux64/bin/java $CDS_COMMAND \
	-client \
	-XX:+UseCompressedOops \
  -Dprism.forceGPU=true \
	-XX:+UseCompressedClassPointers \
	--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
  --add-exports javafx.controls/com.sun.javafx.scene.control.skin=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED \
	--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
  --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED \
	--add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
	-Dprism.dirtyopts=false -cp "$BASE_DIR"/plugins/*:"$BASE_DIR"/milkman.jar milkman.MilkmanApplication
