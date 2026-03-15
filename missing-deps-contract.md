# Missing Dependencies Contract

## Payload Structure

The analysis pipeline returns:

```json
{
  "missingDependencies": [],
  "resolvedDependencies": []
}
```

## missingDependencies

### Fields

| Field | Type | Required |
|------|------|----------|
| id | string | yes |
| name | string | yes |
| requiredVersion | string | optional |
| loader | string | optional |
| mcVersion | string | optional |

Example:

```json
{
  "id": "better-armour",
  "name": "Better Armour",
  "requiredVersion": "1.3.2",
  "loader": "fabric",
  "mcVersion": "1.20.1"
}
```

## resolvedDependencies

### Fields

| Field | Type | Required |
|------|------|----------|
| id | string | yes |
| name | string | yes |
| links | array[string] | yes |
| preferred | string | optional |

Example:

```json
{
  "id": "better-armour",
  "name": "Better Armour",
  "links": [
    "https://modrinth.com/mod/better-armour/version/1.3.2"
  ],
  "preferred": "https://modrinth.com/mod/better-armour/version/1.3.2"
}
```

## Rules

### Ordering

1. exact version
2. compatible version
3. project page

### Deduplication

- dependency ids must be unique
- duplicate links must be removed

## Edge Cases

### No Missing Dependencies

```json
{
  "missingDependencies": [],
  "resolvedDependencies": []
}
```

### Missing Dependencies Without Links

```json
{
  "missingDependencies": [
    { "id": "unknown-mod", "name": "Unknown Mod" }
  ],
  "resolvedDependencies": []
}
```

### Multiple Candidate Links

```json
{
  "id": "fabric-api",
  "name": "Fabric API",
  "links": [
    "https://modrinth.com/mod/fabric-api/version/0.91.0",
    "https://modrinth.com/mod/fabric-api/version/0.90.0"
  ],
  "preferred": "https://modrinth.com/mod/fabric-api/version/0.91.0"
}
```

## JSON Examples

### Happy Path

```json
{
  "missingDependencies": [
    { "id": "better-armour", "name": "Better Armour", "requiredVersion": "1.3.2" },
    { "id": "fabric-api", "name": "Fabric API" }
  ],
  "resolvedDependencies": [
    {
      "id": "better-armour",
      "name": "Better Armour",
      "links": ["https://modrinth.com/mod/better-armour/version/1.3.2"],
      "preferred": "https://modrinth.com/mod/better-armour/version/1.3.2"
    }
  ]
}
```

### No Missing Dependencies

```json
{
  "missingDependencies": [],
  "resolvedDependencies": []
}
```

### Partial Resolution

```json
{
  "missingDependencies": [
    { "id": "better-armour", "name": "Better Armour" },
    { "id": "mystery-mod", "name": "Mystery Mod" }
  ],
  "resolvedDependencies": [
    {
      "id": "better-armour",
      "name": "Better Armour",
      "links": ["https://modrinth.com/mod/better-armour"],
      "preferred": "https://modrinth.com/mod/better-armour"
    }
  ]
}
```