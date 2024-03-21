#!/bin/sh
chmod +x jre-macos64/bin/java
chmod +x jre-macos64/lib/jspawnhelper

# generate static cds
if [ ! -f "./jre-macos64/lib/client/classes.jsa" ]; then
    "./jre-macos64/bin/java" -Xshare:dump
fi

CDS_COMMAND=-XX:SharedArchiveFile=app-cds.jsa
if [ ! -f "app-cds.jsa" ]; then
  CDS_COMMAND=-XX:ArchiveClassesAtExit=app-cds.jsa
fi

./jre-macos64/bin/java $CDS_COMMAND \
	-client -Xmx2G \
	-XX:+UseCompressedOops \
	-XX:+UseCompressedClassPointers \
	--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
  --add-exports javafx.controls/com.sun.javafx.scene.control.skin=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED \
  --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
	--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED \
	--add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
	-Dprism.dirtyopts=false -Dprism.lcdtext=false -cp plugins/*:milkman.jar milkman.MilkmanApplication
