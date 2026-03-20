# QA Checklist - Missing Dependencies Results Page

Use this checklist to test the missing-dependencies results page.

---

## How to Use This Checklist

Any team member familiar with running the app locally can execute this checklist.

### Step 1: Start the App

```bash
./gradlew bootRun
```

Wait for "Started MinecraftProjectApplication" in the console.

### Step 2: Open the Page

Go to: **http://localhost:8080/missing-dependencies**

### Step 3: Test Different Scenarios

Add `?mode=XYZ` to the URL to see different situations:

| To see... | Use this URL |
|-----------|--------------|
| Normal results (some found, some not) | `/missing-dependencies` |
| Everything resolved | `/missing-dependencies?mode=ok` |
| Nothing found | `/missing-dependencies?mode=empty` |
| Partial results (mixed) | `/missing-dependencies?mode=partial` |
| Error: too many requests | `/missing-dependencies?mode=fail429` |
| Error: server problem | `/missing-dependencies?mode=fail500` |

---

## Verification Checklist

### 1. Page Structure

- [ ] Page title says "Missing Dependencies"
- [ ] No red error messages visible on the page
- [ ] Browser console (F12) shows no critical JavaScript errors
- [ ] Page renders without layout breaking or overlapping elements

### 2. Results List Rendering

- [ ] Dependency cards appear when data is available
- [ ] Each card displays: name, version, loader type, Minecraft version
- [ ] Cards are visually distinct and properly separated
- [ ] No card content overlaps with other cards or UI elements

### 3. Resolution Status Indicators

- [ ] When a dependency includes a valid resolved source, the card displays a visible indicator showing resolution status
- [ ] The resolution indicator appears together with an available action button or link
- [ ] Status text is readable and not only color-based
- [ ] When no source is found, the card shows an appropriate indicator

### 4. Download/Open Behavior

- [ ] For resolved dependencies, a clickable action button/link is displayed
- [ ] Clicking the button opens a new browser tab
- [ ] All external links use a safe redirect mechanism
- [ ] Links point only to approved sources (Modrinth, CurseForge) during testing
- [ ] No result card exposes a raw or unexpected destination URL

### 5. Install Guidance Visibility

- [ ] Guidance content is visible alongside or near the results list
- [ ] Guidance does not get hidden behind result cards on desktop view
- [ ] Install guidance is easy to locate without excessive scrolling on desktop
- [ ] Guidance mentions the correct loader type (Fabric/Forge) based on context

### 6. Warnings Display

- [ ] Warning content about compatibility is visible
- [ ] Warning mentions incompatibility between Forge and Fabric
- [ ] Warning mentions Minecraft version mismatches

### 7. Unresolved Dependency Guidance

- [ ] When a dependency cannot be resolved, guidance suggests next steps
- [ ] Guidance suggests searching manually on Modrinth or CurseForge

### 8. Empty State

- [ ] When no missing dependencies exist, an appropriate message is displayed
- [ ] No dependency cards appear in empty state
- [ ] Guidance panels remain visible in empty state

### 9. Partial Results State

- [ ] When some dependencies resolve and others do not, a banner or indicator appears
- [ ] Banner clearly communicates that mixed results occurred
- [ ] At least one card shows resolved status indicator
- [ ] At least one card shows unresolved status indicator

### 10. Error Handling - Rate Limited (429)

- [ ] When rate limited, an error message appears
- [ ] Error message is readable and explains the situation
- [ ] Error message suggests retrying later

### 11. Error Handling - Server Problem (500)

- [ ] When server error occurs, an error message appears
- [ ] Error message is readable and explains the situation

### 12. Loading State

- [ ] When page first loads, a loading indicator is displayed
- [ ] Loading indicator disappears after data loads or error occurs

### 13. Retry / Reload Behavior

- [ ] Reloading the page (browser refresh) works correctly after an error
- [ ] Page does not break or leave stale loading text after reload
- [ ] Reload does not duplicate result cards or guidance panels
- [ ] After following install guidance, user can return to re-run analysis

### 14. Copy / Export (Not Implemented)

- [ ] Copy/Export functionality is NOT implemented in current version (N/A)

### 15. Responsive Layout

- [ ] On narrow viewport (mobile-like), page changes to single column layout
- [ ] Dependency cards stack vertically on narrow viewport
- [ ] No horizontal scrolling is required on narrow viewport
- [ ] Text remains readable on narrow viewport

---

## Quick Verification (30 Seconds)

Run these 5 checks:

1. Page loads without errors → [ ]
2. At least one dependency card shows → [ ]
3. Guidance content visible alongside results → [ ]
4. Download/action button is clickable → [ ]
5. Narrow window does not break layout → [ ]

---

## Test Data Reference

Default mode typically shows: Fabric API, Mod Menu  
Partial mode shows: Fabric API (resolved), Cloth Config (unresolved)

---

## Troubleshooting

**Page won't load:**
- Is the app running? Check console for "Started MinecraftProjectApplication"
- Try http://localhost:8080 first

**No dependencies showing:**
- Make sure you're using the correct URL with ?mode parameter
- Check browser console (F12) for errors

**Guidance panels missing:**
- This is a bug — report it

---

## Notes

- This uses mock data. Real data will come later from the API.
- The `?mode=...` URLs are for testing only — won't work in production.
- Report any issues to the team channel.

---

**For questions or issues:** Ask in #dev-team
**Last updated:** Sprint 1 - Revised
