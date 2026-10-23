#!/bin/sh
chmod +x jre-macos64/bin/java
chmod +x jre-macos64/lib/jspawnhelper

# Per-user, absolute cache path so the AOT cache survives relaunches from any CWD.
APP_DATA="${HOME}/Library/Application Support/Milkman"
mkdir -p "$APP_DATA"
AOT_CACHE="$APP_DATA/milkman.aot"
AOT_CONF="$APP_DATA/milkman.aot.config"

# Invalidate the cache when the shipped milkman.jar changes (new release).
if [ -f "$AOT_CACHE" ] && [ milkman.jar -nt "$AOT_CACHE" ]; then
  rm -f "$AOT_CACHE" "$AOT_CONF"
fi

if [ -f "$AOT_CACHE" ]; then
  AOT_OPT="-XX:AOTCache=$AOT_CACHE"
else
  AOT_OPT="-XX:AOTMode=record -XX:AOTConfiguration=$AOT_CONF"
fi

JAVA=./jre-macos64/bin/java
CP="plugins/*:milkman.jar"
BASE_OPTS="-Xmx2G -XX:+UseCompressedOops \
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
    -Dprism.dirtyopts=false -Dprism.lcdtext=false"

$JAVA $AOT_OPT $BASE_OPTS -cp "$CP" milkman.MilkmanApplication

# After a record run, materialize the AOT cache in the background so the next
# launch gets it. Harmless if it fails; next launch will just record again.
if [ -f "$AOT_CONF" ] && [ ! -f "$AOT_CACHE" ]; then
  nohup $JAVA -XX:AOTMode=create -XX:AOTConfiguration="$AOT_CONF" -XX:AOTCache="$AOT_CACHE" \
    $BASE_OPTS -cp "$CP" milkman.MilkmanApplication >/dev/null 2>&1 &
fi
