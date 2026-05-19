# Modpack de prueba (Fabric)

## Usar `sample-fabric-modpack.zip`

Ruta en el repo:

```
test-fixtures/sample-fabric-modpack.zip
```

Contiene:

- `mods/fabric-api.jar`
- `mods/example-mod.jar` (depende de `fabric-api`)

### Prueba manual

1. `./gradlew bootRun`
2. Página de upload → sube **este archivo** (no un `.jar` suelto)
3. Deberías ir a `/graph`

### Regenerar el zip

```bash
./test-fixtures/create-sample-modpack.sh
```

## Si ves `zip END header not found`

Causas habituales:

1. **Subiste un `.jar` renombrado a `.zip`** → debe ser un zip que **contenga** la carpeta `mods/` con jars dentro.
2. **Comprimiste mal en macOS** (Finder añade `__MACOSX` y archivos `._*` que rompían el parser; ya se ignoran en código nuevo).
3. **Zip corrupto o incompleto** → usa el fixture del repo o el script de arriba.

Estructura correcta:

```
mi-modpack.zip
└── mods/
    ├── fabric-api.jar
    └── example-mod.jar
```

**No** comprimas la carpeta padre del proyecto entero; solo el contenido del modpack con `mods/` en la raíz del zip.
