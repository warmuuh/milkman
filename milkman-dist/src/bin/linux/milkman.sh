#!/bin/sh
chmod +x jre-linux64/bin/java

./jre-linux64/bin/java --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED \
	--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
	--add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
	--add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED \
	--add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
	-Dprism.dirtyopts=false -cp plugins/*:milkman.jar milkman.MilkmanApplication
