#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD="$ROOT/build"
OUT="$ROOT/sample-fabric-modpack.zip"

rm -rf "$BUILD" "$OUT"
mkdir -p "$BUILD/mods" "$BUILD/jar1" "$BUILD/jar2"

cat > "$BUILD/jar1/fabric.mod.json" <<'EOF'
{"schemaVersion":1,"id":"fabric-api","version":"0.92.2+1.20.1","name":"Fabric API","environment":"*","depends":{}}
EOF

cat > "$BUILD/jar2/fabric.mod.json" <<'EOF'
{"schemaVersion":1,"id":"example-mod","version":"1.0.0","name":"Example Mod","environment":"*","depends":{"fabric-api":"*"}}
EOF

(cd "$BUILD/jar1" && zip -q "$BUILD/mods/fabric-api.jar" fabric.mod.json)
(cd "$BUILD/jar2" && zip -q "$BUILD/mods/example-mod.jar" fabric.mod.json)
(cd "$BUILD" && zip -q "$OUT" mods/fabric-api.jar mods/example-mod.jar)

rm -rf "$BUILD"
echo "Created: $OUT"
unzip -l "$OUT"
