#!/usr/bin/env sh

BASE_DIR="$(cd "$(dirname "$0")"; pwd)" || exit 2
chmod +x "$BASE_DIR"/jre-linux64/bin/java

# Per-user, absolute cache path so the AOT cache survives relaunches from any CWD.
APP_DATA="${XDG_DATA_HOME:-$HOME/.local/share}/milkman"
mkdir -p "$APP_DATA"
AOT_CACHE="$APP_DATA/milkman.aot"
AOT_CONF="$APP_DATA/milkman.aot.config"

if [ -f "$AOT_CACHE" ] && [ "$BASE_DIR/milkman.jar" -nt "$AOT_CACHE" ]; then
  rm -f "$AOT_CACHE" "$AOT_CONF"
fi

if [ -f "$AOT_CACHE" ]; then
  set -- "-XX:AOTCache=$AOT_CACHE"
else
  set -- "-XX:AOTMode=record" "-XX:AOTConfiguration=$AOT_CONF"
fi

JAVA="$BASE_DIR/jre-linux64/bin/java"
CP="$BASE_DIR/plugins/*:$BASE_DIR/milkman.jar"
BASE_OPTS="-Xmx2G -XX:+UseCompressedOops -Dprism.forceGPU=true \
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
    -Dprism.dirtyopts=false"

"$JAVA" "$@" $BASE_OPTS -cp "$CP" milkman.MilkmanApplication

if [ -f "$AOT_CONF" ] && [ ! -f "$AOT_CACHE" ]; then
  nohup "$JAVA" -XX:AOTMode=create -XX:AOTConfiguration="$AOT_CONF" -XX:AOTCache="$AOT_CACHE" \
    $BASE_OPTS -cp "$CP" milkman.MilkmanApplication >/dev/null 2>&1 &
fi
