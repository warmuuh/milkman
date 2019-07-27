#!/usr/bin/env sh

BASE_DIR="$(cd "$(dirname "$0")"; pwd)" || exit 2

mkdir -p $HOME/.local/share/applications

cat <<EOF > $HOME/.local/share/applications/Milkman.desktop
[Desktop Entry]
Name=Milkman
Exec=$BASE_DIR/milkman.sh
Icon=$BASE_DIR/milk-bottle.png
Terminal=false
Type=Application
Keywords=milkman;postman;rest;http;sql;jdbc;java;
EOF
