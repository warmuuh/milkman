#!/bin/sh
chmod +x jre-macos64/bin/java

./jre-macos64/bin/java -XX:SharedArchiveFile=app-cds.jsa \
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
	-Dprism.dirtyopts=false -cp plugins/*:milkman.jar milkman.MilkmanApplication
