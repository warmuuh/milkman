@echo off

if not exist .\jre-win64\lib\client\classes.jsa (
    .\jre-win64\bin\java.exe -Xshare:dump
)

set CDS_COMMAND=-XX:SharedArchiveFile=app-cds.jsa
if not exist "app-cds.jsa" (
  set CDS_COMMAND=-XX:ArchiveClassesAtExit=app-cds.jsa
)

@start .\jre-win64\bin\javaw.exe ^
	%CDS_COMMAND% ^
	-client -Xmx2G ^
	-XX:+UseCompressedOops ^
	-XX:+UseCompressedClassPointers ^
	--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED ^
	--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
    --add-exports javafx.controls/com.sun.javafx.scene.control.skin=ALL-UNNAMED ^
	--add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED ^
	--add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED ^
	--add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED ^
	--add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED ^
	--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED ^
	--add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED ^
	--add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
	--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
	--add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED ^
	--add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED ^
	--add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
	-Dprism.dirtyopts=false -cp plugins\*;milkman.jar milkman.MilkmanApplication
