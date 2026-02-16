# JustLeveling Fork + KubeJS - Legacy Style Guide (Forge 1.20.1)

> Target: pack-devs that want a 1.12.2-style reference (`Syntax`, `Blank Example`, `Working Example`, `Setters/Getters`) with the real APIs available in this fork.

## Overview / PSA

This guide documents only features that currently exist in this repo for JLFork 1.20.1 + KubeJS.

Scope of this guide:

- Custom aptitudes
- Custom skills
- Custom passives
- Custom titles + title conditions
- Item requirement locks (item and mod)
- Aptitude level locks
- Aptitude visibility locks
- NBT locks
- Transmutations (reagent + block/blockstate)
- Level-up command hooks
- Runtime player control API
- JLForkEvents level-up events

Out of scope in this fork:

- Traits / unlockables API like old CompatSkills 1.12.2

PSA about event bypasses:

- `JLForkEvents.aptitudeLevelUpServer` is fired when leveling through the normal aptitude level-up packet flow.
- If another system changes aptitude levels directly (`PlayerDataAPI.setAptitudeLevel`, command logic, direct Java calls), that event is not guaranteed to fire.
- For strict server enforcement, validate in server logic as well (not only client event).

## Index

1. [Bindings Available](#bindings-available)
2. [Bracket Handlers / Lookup Equivalents](#bracket-handlers--lookup-equivalents)
3. [Custom Aptitudes](#custom-aptitudes)
4. [Custom Skills](#custom-skills)
5. [Custom Passives](#custom-passives)
6. [Custom Titles + Conditions](#custom-titles--conditions)
7. [Requirement Tweaker (modern)](#requirement-tweaker-modern)
8. [Level-Lock Support (modern)](#level-lock-support-modern)
9. [Visibility Locking (modern)](#visibility-locking-modern)
10. [NBT-Lock Tweaker (modern)](#nbt-lock-tweaker-modern)
11. [Transmutations (modern)](#transmutations-modern)
12. [Skill Change Tweaker (modern)](#skill-change-tweaker-modern)
13. [Player Data API (admin/runtime)](#player-data-api-adminruntime)
14. [Events](#events)
15. [Full Scale Example](#full-scale-example)
16. [Troubleshooting / Edge Cases](#troubleshooting--edge-cases)

## Bindings Available

These are registered by `Plugin.registerBindings`:

- `ValueType`
- `Value`
- `Aptitude`
- `SkillCreator`
- `AbilityCreator`
- `PassiveCreator`
- `LegacySkill`
- `Skill`
- `Passive`
- `Title`
- `TitleAPI`
- `TitleComparator`
- `LockItemAPI`
- `NBTLockAPI`
- `LevelLockAPI`
- `VisibilityLockAPI`
- `TransmutationAPI`
- `SkillChangeAPI`
- `PlayerDataAPI`

## Bracket Handlers / Lookup Equivalents

There is no CraftTweaker bracket handler syntax in this API. Use direct class lookups.

### Syntax

```js
Aptitude.getByName(String nameOrId)
SkillCreator.get(String nameOrId)
AbilityCreator.get(String nameOrId)
PassiveCreator.get(String nameOrId)
Title.getByName(String name)
```

### Blank Example

```js
var aptitude = Aptitude.getByName('my_aptitude')
var skill = AbilityCreator.get('my_skill')
var passive = PassiveCreator.get('my_passive')
var title = Title.getByName('my_title')
```

### Working Example

```js
var strength = Aptitude.getByName('strength')
var oneHanded = AbilityCreator.get('one_handed')
var luckPassive = PassiveCreator.get('luck')
var titleless = Title.getByName('titleless')
```

### Methods / Setters / Getters

| API | Method | Args | Return | Behavior |
|---|---|---|---|---|
| `Aptitude` | `getByName` | `name` | `Aptitude or null` | Accepts path or namespaced id. |
| `SkillCreator` | `get` | `nameOrId` | `LegacySkill or null` | Returns compatibility wrapper around aptitude lookup. |
| `AbilityCreator` | `get` | `nameOrId` | `Skill or null` | Reads registered or pending skills. |
| `PassiveCreator` | `get` | `nameOrId` | `Passive or null` | Reads registered or pending passives. |
| `Title` | `getByName` | `name` | `Title or null` | Name-based title lookup. |

PSA:

- IDs are normalized to lower-case internally.
- For namespaced content, prefer explicit ids like `my_mod:my_name`.

## Custom Aptitudes

### Syntax

```js
Aptitude.add(String name, String background, String... lockedTextures)
Aptitude.add(String name, String background, int backgroundRepeat, String... lockedTextures)
Aptitude.addWithId(String nameOrId, String background, String... lockedTextures)
Aptitude.addWithId(String nameOrId, String background, int backgroundRepeat, String... lockedTextures)
Aptitude.addWithId(ResourceLocation id, String background, String... lockedTextures)
Aptitude.addWithId(ResourceLocation id, String background, int backgroundRepeat, String... lockedTextures)
Aptitude.getByName(String nameOrId)
```

### Blank Example

```js
var apt = Aptitude.add('my_aptitude', 'minecraft:textures/block/stone.png', 'kubejs:textures/item/example_item.png')
apt.setLevelCap(32)
apt.setBaseLevelCost(3)
apt.setSkillPointInterval(4)
apt.setLevelStaggering('1|1', '8|2', '16|3')
apt.setEnabled(true)
apt.setHidden(false)
```

### Working Example

```js
var smithing = Aptitude.addWithId(
  'my_pack:smithing',
  'minecraft:textures/block/stone_bricks.png',
  2,
  'kubejs:textures/item/example_item.png',
  'kubejs:textures/item/example_item.png',
  'kubejs:textures/item/example_item.png',
  'kubejs:textures/item/example_item.png'
)

smithing.setDisplayNameOverride('Smithing')
smithing.setLevelCap(64)
smithing.setBaseLevelCost(2)
smithing.setSkillPointInterval(4)
smithing.setLevelStaggering('1|1', '8|2', '16|3', '32|1', '48|0')
smithing.setBackgroundRepeat(4)
smithing.setRankIcon(0, 'minecraft:textures/item/iron_ingot.png')
smithing.setRankIcon(4, 'minecraft:textures/item/diamond.png')
smithing.setRankIcon(8, 'minecraft:textures/item/nether_star.png')
smithing.setEnabled(true)
smithing.setHidden(false)
```

### Methods / Setters / Getters

| API | Method | Type | Args | Return | Behavior / Constraints |
|---|---|---|---|---|---|
| `Aptitude` | `add` | static | `name, background, lockedTextures...` | `Aptitude` | `name` uses default mod namespace. At least one locked texture required. |
| `Aptitude` | `addWithId` | static | `nameOrId/id, background, [backgroundRepeat], lockedTextures...` | `Aptitude` | Use this for namespaced ids. |
| `Aptitude` | `getByName` | static | `nameOrId` | `Aptitude or null` | Lookup from pending + registry. |
| `Aptitude` | `setLevelCap/getLevelCap` | set/get | `int` / - | `void` / `int` | Per-aptitude cap. Getter falls back to config cap when unset. |
| `Aptitude` | `setBaseLevelCost/getBaseLevelCost` | set/get | `int` / - | `void` / `int` | Base level cost; getter falls back to config default when unset. |
| `Aptitude` | `setSkillPointInterval/getSkillPointInterval` | set/get | `int` / - | `void` / `int` | Interval clamp min `1`. |
| `Aptitude` | `setLevelStaggering/getLevelStaggering` | set/get | `String...` / - | `void` / `String[]` | Format `level|delta`, invalid entries ignored. |
| `Aptitude` | `setEnabled/isEnabled` | set/get | `boolean` / - | `void` / `boolean` | Enables/disables aptitude systems. |
| `Aptitude` | `setHidden/isHidden` | set/get | `boolean` / - | `void` / `boolean` | Hides aptitude from list UI. |
| `Aptitude` | `setDisplayNameOverride/getDisplayNameOverride` | set/get | `String` / - | `void` / `String` | Optional display override. |
| `Aptitude` | `getDisplayNameOrFallback` | getter | - | `String` | Localized or generated fallback. |
| `Aptitude` | `getAbbreviationOrFallback` | getter | - | `String` | Localized `.abbreviation` or fallback. |
| `Aptitude` | `setBackgroundRepeat/getBackgroundRepeat` | set/get | `int` / - | `void` / `int` | Clamp `0..64`. |
| `Aptitude` | `setRankIcon/removeRankIcon` | setter | `rank, texture` / `rank` | `void` | Rank range `0..8`. |
| `Aptitude` | `setLockedTextures` | setter | `textures...` | `void` | No-op when empty input. |
| `Aptitude` | `getLevel/getLevel(player)` | getter | - / `Player` | `int` | Current aptitude level. |
| `Aptitude` | `getLevelUpExperienceLevels` | method | `aptitudeLevel` | `int` | XP levels required for next upgrade. |
| `Aptitude` | `getLevelUpPointCost` | method | `aptitudeLevel` | `int` | XP points required for next upgrade. |
| `Aptitude` | `getSkills/getPassives` | method | `aptitude` | `List` | Advanced helper methods. |

### Localization & Resource Location References

Localization files:

- `kubejs/assets/<namespace>/lang/en_us.json`

Aptitude keys:

- Name: `aptitude.<namespace>.<path>`
- Abbreviation: `aptitude.<namespace>.<path>.abbreviation`
- Description: `aptitude.<namespace>.<path>.description`

PSA:

- `Aptitude.add` uses default namespace (`justlevelingfork`).
- Use `addWithId` for external namespace ownership.

## Custom Skills

### Syntax

```js
Skill.add(String skillName, String aptitudeName, int levelRequirement, String texture, Value... values)
Skill.addWithId(String skillNameOrId, String aptitudeName, int levelRequirement, String texture, Value... values)
Skill.addWithId(ResourceLocation id, String aptitudeName, int levelRequirement, String texture, Value... values)

new Value(ValueType type, Object value)
```

### Blank Example

```js
var skill = Skill.add(
  'my_skill',
  'my_aptitude',
  3,
  'my_pack:textures/skill/my_skill.png',
  new Value(ValueType.PERCENT, 12.0)
)
skill.setPointCost(2)
```

### Working Example

```js
var focus = Skill.addWithId(
  'my_pack:smithing_focus',
  'my_pack:smithing',
  4,
  'textures/skill/building/convergence.png',
  new Value(ValueType.MODIFIER, 1.25),
  new Value(ValueType.PERCENT, 8.0)
)
focus.setPointCost(3)
```

### Methods / Setters / Getters

| API | Method | Type | Args | Return | Behavior / Constraints |
|---|---|---|---|---|---|
| `Skill` | `add` | static | `name, aptitude, requiredLevel, texture, values...` | `Skill` | Default namespace helper. |
| `Skill` | `addWithId` | static | `nameOrId/id, aptitude, requiredLevel, texture, values...` | `Skill` | Namespaced helper. |
| `Skill` | `getLvl` | getter | - | `int` | Aptitude requirement to unlock/toggle. |
| `Skill` | `setPointCost/getPointCost` | set/get | `int` / - | `void` / `int` | Cost clamp min `1`. |
| `Skill` | `setSpCost/getSpCost` | set/get | `int` / - | `void` / `int` | Alias of point cost methods. |
| `Skill` | `getValue` | getter | - | `double[]` | Numeric view of configured `Value[]`. |
| `Skill` | `canSkill` | method | - / `(Player)` | `boolean` | True when requirement met, unlocked, and toggled. |
| `Skill` | `getToggle` | getter | - / `(Player)` | `boolean` | Requirement gate for UI logic. |
| `Skill` | `isEnabled` | method | - / `(Player)` | `boolean` | Runtime final state used by effects. |
| `ValueType` | enum values | enum | - | - | `MODIFIER`, `DURATION`, `AMPLIFIER`, `PERCENT`, `BOOST`, `PROBABILITY`. |
| `Value` | ctor | class | `type, value` | `Value` | Value wrapper for skill descriptions and behavior. |

### Localization & Resource Location References

Localization files:

- `kubejs/assets/<namespace>/lang/en_us.json`

Skill keys:

- Name: `skill.<namespace>.<path>`
- Description: `skill.<namespace>.<path>.description`

PSA:

- `requiredLevel <= 0` makes normal enable flow fail requirement checks.
- SP is spent on unlock, not on each toggle flip.

## Custom Passives

### Syntax

```js
Passive.add(String passiveName, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired)
Passive.addWithId(String passiveNameOrId, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired)
Passive.addWithId(ResourceLocation id, String aptitudeName, String texture, Attribute attribute, String attributeUUID, Object attributeValue, int... levelsRequired)
Passive.add(String passiveName, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired)
Passive.addByAttributeId(String passiveName, String aptitudeName, String texture, String attributeId, String attributeUUID, Object attributeValue, int... levelsRequired)
```

### Blank Example

```js
var passive = Passive.addByAttributeId(
  'my_passive',
  'my_aptitude',
  'my_pack:textures/passive/my_passive.png',
  'minecraft:generic.luck',
  '11111111-2222-3333-4444-555555555555',
  2.0,
  5, 10, 15, 20
)
passive.setPointCost(2)
```

### Working Example

```js
var speedPassive = Passive.addWithId(
  'my_pack:throwing_speed',
  'my_pack:smithing',
  'textures/skill/dexterity/passive_movement_speed.png',
  'minecraft:generic.movement_speed',
  'd98c84f0-cce4-42f5-a40f-35eb4e89c00a',
  0.12,
  3, 6, 9, 12
)
speedPassive.setPointCost(3)
```

### Methods / Setters / Getters

| API | Method | Type | Args | Return | Behavior / Constraints |
|---|---|---|---|---|---|
| `Passive` | `add` | static | `name, aptitude, texture, attribute/object, levels...` | `Passive` | Default namespace helper. |
| `Passive` | `addWithId` | static | `nameOrId/id, aptitude, texture, attribute/object, levels...` | `Passive` | Namespaced helper. |
| `Passive` | `addByAttributeId` | static | `name, aptitude, texture, attributeId, uuid, value, levels...` | `Passive` | Recommended for KubeJS scripts. |
| `Passive` | `setPointCost/getPointCost` | set/get | `int` / - | `void` / `int` | Cost clamp min `1`. |
| `Passive` | `setSpCost/getSpCost` | set/get | `int` / - | `void` / `int` | Alias of point cost methods. |
| `Passive` | `getMaxLevel` | getter | - | `int` | Equals `levelsRequired.length`. |
| `Passive` | `getLevel/getLevel(player)` | getter | - / `Player` | `int` | Current passive level. |
| `Passive` | `getNextLevelUp` | method | - | `int` | Aptitude requirement for next passive level. |
| `Passive` | `getValue` | getter | - | `double` | Total passive value at max level. |

### Gating Behavior (important)

- Passive level-up validates aptitude requirements per level step.
- If requirements fail, level does not increase.
- GUI flow refunds SP when no passive level increase happened.
- GUI passive downgrade is removed in this fork, but admin/runtime APIs can still reduce levels.

### Localization & Resource Location References

Localization files:

- `kubejs/assets/<namespace>/lang/en_us.json`

Passive keys:

- Name: `passive.<namespace>.<path>`
- Description: `passive.<namespace>.<path>.description`

## Custom Titles + Conditions

### Syntax

```js
Title.add(String name)
Title.add(String name, boolean defaultUnlocked, boolean hideRequirements)
Title.getByName(String name)

TitleAPI.conditions(String titleName)
  .aptitude(String aptitudeName, TitleComparator comparator, int value)
  .stat(String statId, TitleComparator comparator, int value)
  .entityKilled(String entityId, TitleComparator comparator, int value)
  .special(String key, TitleComparator comparator, String value)
  .register()

TitleAPI.clearConditions(String titleName)
```

### Blank Example

```js
Title.add('my_title', false, false)
TitleAPI.clearConditions('my_title')
TitleAPI.conditions('my_title')
  .aptitude('magic', TitleComparator.GREATER_OR_EQUAL, 10)
  .register()
```

### Working Example

```js
Title.add('archmage_test', false, false)
Title.add('shadow_agent_test', false, true)

TitleAPI.clearConditions('archmage_test')
TitleAPI.conditions('archmage_test')
  .aptitude('magic', TitleComparator.GREATER_OR_EQUAL, 10)
  .stat('minecraft:jump', TitleComparator.GREATER_OR_EQUAL, 250)
  .register()
```

### Methods / Setters / Getters

| API | Method | Type | Args | Return | Behavior / Constraints |
|---|---|---|---|---|---|
| `Title` | `add` | static | `name` | `Title` | Same as `add(name, false, false)`. |
| `Title` | `add` | static | `name, defaultUnlocked, hideRequirements` | `Title` | Creates title in default namespace. |
| `Title` | `getByName` | static | `name` | `Title or null` | Title lookup by normalized name. |
| `Title` | `getRequirement/getRequirement(player)` | getter | - / `Player` | `boolean` | Unlock state. |
| `TitleAPI` | `conditions` | static | `titleName` | `ConditionBuilder` | Starts fluent builder. |
| `TitleAPI` | `clearConditions` | static | `titleName` | `void` | Clears KubeJS conditions for that title. |
| `ConditionBuilder` | `aptitude` | chain | `aptitude, comparator, value` | `ConditionBuilder` | Adds aptitude condition. |
| `ConditionBuilder` | `stat` | chain | `statId, comparator, value` | `ConditionBuilder` | Adds custom stat condition. |
| `ConditionBuilder` | `entityKilled` | chain | `entityId, comparator, value` | `ConditionBuilder` | Adds entity killed condition. |
| `ConditionBuilder` | `special` | chain | `key, comparator, value` | `ConditionBuilder` | Adds special condition (dimension-style use). |
| `ConditionBuilder` | `register` | terminal | - | `void` | Persists condition set in title registry integration map. |
| `TitleComparator` | enum | enum | - | - | `EQUALS`, `GREATER`, `LESS`, `GREATER_OR_EQUAL`, `LESS_OR_EQUAL`. |

PSA:

- Conditions are internally stored as `type/variable/comparator/value` strings.
- Title conditions are evaluated server-side continuously by title sync logic.

## Requirement Tweaker (modern)

This is the modern replacement for old requirement/mod-lock style from 1.12.2.

### Syntax

```js
LockItemAPI.clearAll()
LockItemAPI.addLock(String itemId, String aptitude, int level)
LockItemAPI.addLock(String itemId, String... requirements)
LockItemAPI.addLock(String itemId, Map<?, ?> requirements)
LockItemAPI.removeLock(String itemId)
LockItemAPI.addModLock(String modId, String aptitude, int level)
LockItemAPI.addModLock(String modId, String... requirements)
LockItemAPI.addModLock(String modId, Map<?, ?> requirements)
```

### Blank Example

```js
LockItemAPI.addLock('minecraft:diamond_sword', 'strength', 12)
LockItemAPI.addModLock('minecraft', 'building', 3)
```

### Working Example

```js
var JavaHashMap = Java.loadClass('java.util.HashMap')

var elytraReq = new JavaHashMap()
elytraReq.put('dexterity', 30)
elytraReq.put('magic', 10)
LockItemAPI.addLock('minecraft:elytra', elytraReq)

var modReq = new JavaHashMap()
modReq.put('strength', 2)
LockItemAPI.addModLock('minecraft', modReq)

LockItemAPI.removeLock('minecraft:fishing_rod')
```

### Methods / Setters / Getters

| API | Method | Args | Return | Behavior / Constraints |
|---|---|---|---|---|
| `LockItemAPI` | `clearAll` | - | `void` | Clears item locks, mod locks, removed locks, and NBT locks; invalidates cache. |
| `LockItemAPI` | `addLock` | `itemId, aptitude, level` | `void` | Adds one-item lock with one requirement. |
| `LockItemAPI` | `addLock` | `itemId, requirements...` | `void` | Legacy-like string overload (`aptitude|level`). |
| `LockItemAPI` | `addLock` | `itemId, requirementsMap` | `void` | Item lock map overload. Empty map removes item lock entry. |
| `LockItemAPI` | `removeLock` | `itemId` | `void` | Explicitly removes lock for one item id. |
| `LockItemAPI` | `addModLock` | `modId, aptitude, level` | `void` | Baseline lock for all items in a namespace. |
| `LockItemAPI` | `addModLock` | `modId, requirements...` | `void` | Legacy-like string overload (`aptitude|level`). |
| `LockItemAPI` | `addModLock` | `modId, requirementsMap` | `void` | Mod lock map overload. Empty map removes mod entry. |
| `LockItemAPI` | `isClearDefaults` | - | `boolean` | Advanced status method. |
| `LockItemAPI` | `getItemLocks/getModLocks/getRemovedLocks` | - | `Map/Set` | Advanced debug snapshots. |

### Precedence and Merge Rules

Final lock resolution order:

1. Default config locks (unless `clearAll()` was called)
2. Mod locks (`addModLock`) as baseline
3. Explicit removals (`removeLock`)
4. Item locks (`addLock`) override everything for that item

PSA:

- `clearAll()` also clears NBT locks by design.
- For map overloads in KubeJS, Java `HashMap` is the safest bridge option.
- String requirements must use `aptitude|level` (example: `'strength|12'`).

## Level-Lock Support (modern)

Gates aptitude progression on specific target levels.

### Syntax

```js
LevelLockAPI.addLevelLock(String aptitude, int targetLevel, String... requirements)
LevelLockAPI.addLevelLock(String aptitude, int targetLevel, Map<?, ?> requirements)
LevelLockAPI.removeLevelLock(String aptitude, int targetLevel)
LevelLockAPI.clearLevelLocks(String aptitude)
LevelLockAPI.clearAllLevelLocks()
LevelLockAPI.getLevelLocks()
LevelLockAPI.canReachLevel(Player player, String aptitude, int targetLevel)
```

### Blank Example

```js
LevelLockAPI.addLevelLock('magic', 10, 'intelligence|8')
```

### Working Example

```js
var JavaHashMap = Java.loadClass('java.util.HashMap')
var req = new JavaHashMap()
req.put('intelligence', 12)
req.put('building', 6)

LevelLockAPI.addLevelLock('magic', 16, req)
LevelLockAPI.addLevelLock('magic', 20, 'intelligence|16', 'luck|10')
```

### Methods / Setters / Getters

| API | Method | Args | Return | Behavior / Constraints |
|---|---|---|---|---|
| `LevelLockAPI` | `addLevelLock` | `aptitude, targetLevel, requirements...` | `void` | String format `aptitude|level`; merged by `max`. |
| `LevelLockAPI` | `addLevelLock` | `aptitude, targetLevel, map` | `void` | Map requirements merged by `max`. |
| `LevelLockAPI` | `removeLevelLock` | `aptitude, targetLevel` | `void` | Removes only that level gate. |
| `LevelLockAPI` | `clearLevelLocks` | `aptitude` | `void` | Removes all level locks for one aptitude. |
| `LevelLockAPI` | `clearAllLevelLocks` | - | `void` | Clears all aptitude level locks. |
| `LevelLockAPI` | `getLevelLocks` | - | `Map` | Deep unmodifiable snapshot for debug/admin. |
| `LevelLockAPI` | `canReachLevel` | `player, aptitude, targetLevel` | `boolean` | Runtime checker used by packet/API/commands. |

PSA:

- En este fork, el enforcement aplica en GUI packet flow, `PlayerDataAPI` y `/aptitudes`.
- Bajadas de nivel siguen permitidas.

## Visibility Locking (modern)

Hides aptitudes in GUI until requirements are met.

### Syntax

```js
VisibilityLockAPI.addVisibilityLock(String aptitude, String... requirements)
VisibilityLockAPI.addVisibilityLock(String aptitude, Map<?, ?> requirements)
VisibilityLockAPI.removeVisibilityLock(String aptitude)
VisibilityLockAPI.clearAllVisibilityLocks()
VisibilityLockAPI.isVisible(Player player, String aptitude)
VisibilityLockAPI.getVisibilityLocks()
```

### Blank Example

```js
VisibilityLockAPI.addVisibilityLock('magic', 'intelligence|6')
```

### Working Example

```js
var JavaHashMap = Java.loadClass('java.util.HashMap')
var showReq = new JavaHashMap()
showReq.put('strength', 4)
showReq.put('intelligence', 8)

VisibilityLockAPI.addVisibilityLock('magic', showReq)
```

### Methods / Setters / Getters

| API | Method | Args | Return | Behavior / Constraints |
|---|---|---|---|---|
| `VisibilityLockAPI` | `addVisibilityLock` | `aptitude, requirements...` | `void` | String format `aptitude|level`. |
| `VisibilityLockAPI` | `addVisibilityLock` | `aptitude, map` | `void` | Map requirements; empty map removes lock. |
| `VisibilityLockAPI` | `removeVisibilityLock` | `aptitude` | `void` | Removes one aptitude visibility lock. |
| `VisibilityLockAPI` | `clearAllVisibilityLocks` | - | `void` | Clears all visibility locks. |
| `VisibilityLockAPI` | `isVisible` | `player, aptitude` | `boolean` | Used by aptitude list/aptitude page visibility. |
| `VisibilityLockAPI` | `getVisibilityLocks` | - | `Map` | Unmodifiable snapshot for debug/admin. |

PSA:

- Effect is visibility only; no automatic respec/reset.

## NBT-Lock Tweaker (modern)

### Syntax

```js
NBTLockAPI.addItemNBTLock(String itemId, String snbt, String... requirements)
NBTLockAPI.addItemNBTLock(String itemId, String snbt, Map<?, ?> requirements)
NBTLockAPI.addModNBTLock(String modId, String snbt, String... requirements)
NBTLockAPI.addModNBTLock(String modId, String snbt, Map<?, ?> requirements)
NBTLockAPI.addGenericNBTLock(String snbt, String... requirements)
NBTLockAPI.addGenericNBTLock(String snbt, Map<?, ?> requirements)
NBTLockAPI.removeItemNBTLocks(String itemId)
NBTLockAPI.removeModNBTLocks(String modId)
NBTLockAPI.clearAllNBTLocks()
```

### Blank Example

```js
NBTLockAPI.addGenericNBTLock('{GlobalGate:1b}', reqMap({ luck: 6 }))
```

### Working Example

```js
var JavaHashMap = Java.loadClass('java.util.HashMap')

function reqMap(obj) {
  var m = new JavaHashMap()
  Object.keys(obj).forEach(k => m.put(String(k), obj[k]))
  return m
}

NBTLockAPI.addItemNBTLock(
  'minecraft:stick',
  '{TraitLock:1b}',
  reqMap({ strength: 7, magic: 4 })
)

NBTLockAPI.addModNBTLock(
  'minecraft',
  '{ModGate:1b}',
  reqMap({ building: 4 })
)

NBTLockAPI.addGenericNBTLock(
  '{GlobalGate:1b}',
  reqMap({ luck: 6 })
)
```

### Methods / Setters / Getters

| API | Method | Args | Return | Behavior / Constraints |
|---|---|---|---|---|
| `NBTLockAPI` | `addItemNBTLock` | `itemId, snbt, requirements...` | `void` | Legacy-like string overload (`aptitude|level`). |
| `NBTLockAPI` | `addItemNBTLock` | `itemId, snbt, map` | `void` | Adds NBT lock scoped to one item id. |
| `NBTLockAPI` | `addModNBTLock` | `modId, snbt, requirements...` | `void` | Legacy-like string overload (`aptitude|level`). |
| `NBTLockAPI` | `addModNBTLock` | `modId, snbt, map` | `void` | Adds NBT lock scoped to one mod namespace. |
| `NBTLockAPI` | `addGenericNBTLock` | `snbt, requirements...` | `void` | Legacy-like string overload (`aptitude|level`). |
| `NBTLockAPI` | `addGenericNBTLock` | `snbt, map` | `void` | Adds NBT lock checked against all items. |
| `NBTLockAPI` | `removeItemNBTLocks` | `itemId` | `void` | Removes all NBT rules for item id. |
| `NBTLockAPI` | `removeModNBTLocks` | `modId` | `void` | Removes all NBT rules for mod namespace. |
| `NBTLockAPI` | `clearAllNBTLocks` | - | `void` | Clears all NBT lock rules. |
| `NBTLockAPI` | `getMatchingRequirements` | `ItemStack` | `Map<String,Integer>` | Advanced runtime resolver used by lock handler. |

### Exact NBT Match Rules

- CompoundTag: subset recursive match
- ListTag: exact same size and index-by-index match
- Primitive tags: exact value equality

### Merge with Plain Locks

When evaluating an item stack:

- Plain lock requirements and matching NBT requirements are merged by aptitude key.
- Final required level per aptitude is `max(plain, nbt)`.

PSA:

- `snbt` must parse into a non-empty `CompoundTag`.
- Invalid SNBT throws `IllegalArgumentException` on registration.

## Transmutations (modern)

Modern transmutation map with reagent-specific and reagent-agnostic entries.

### Syntax

```js
TransmutationAPI.setConsumeReagent(boolean consume)
TransmutationAPI.getConsumeReagent()
TransmutationAPI.addEntryToReagent(String reagentItemId, String startState, String endState)
TransmutationAPI.addEntryToReagent(String reagentItemId, String startState, String endState, String requiredSkill)
TransmutationAPI.addEntryToReagentAgnostic(String startState, String endState)
TransmutationAPI.addEntryToReagentAgnostic(String startState, String endState, String requiredSkill)
TransmutationAPI.removeStartStateFromReagent(String reagentItemId, String startState)
TransmutationAPI.removeStartStateReagentAgnostic(String startState)
TransmutationAPI.removeEndStateFromReagent(String reagentItemId, String endState)
TransmutationAPI.removeEndStateReagentAgnostic(String endState)
TransmutationAPI.clearMapOfReagent(String reagentItemId)
TransmutationAPI.clearReagentOfEntries(String reagentItemId)
TransmutationAPI.clearReagentMap()
TransmutationAPI.getRulesSnapshot()
```

### Blank Example

```js
TransmutationAPI.addEntryToReagent(
  'minecraft:chorus_fruit',
  'minecraft:melon',
  'minecraft:pumpkin'
)
```

### Working Example

```js
TransmutationAPI.setConsumeReagent(true)

TransmutationAPI.addEntryToReagent(
  'minecraft:chorus_fruit',
  'minecraft:oak_log[axis=x]',
  'minecraft:spruce_log[axis=x]'
)

TransmutationAPI.addEntryToReagentAgnostic(
  'minecraft:stone',
  'minecraft:end_stone',
  'convergence'
)
```

### Methods / Setters / Getters

| API | Method | Args | Return | Behavior / Constraints |
|---|---|---|---|---|
| `TransmutationAPI` | `setConsumeReagent/getConsumeReagent` | `boolean` / - | `void` / `boolean` | Global reagent consumption toggle. |
| `TransmutationAPI` | `addEntryToReagent` | `reagent, start, end[, requiredSkill]` | `void` | Adds reagent-specific rule. |
| `TransmutationAPI` | `addEntryToReagentAgnostic` | `start, end[, requiredSkill]` | `void` | Adds rule valid for any reagent item. |
| `TransmutationAPI` | `removeStartStateFromReagent` | `reagent, start` | `void` | Removes reagent rule(s) by start state. |
| `TransmutationAPI` | `removeStartStateReagentAgnostic` | `start` | `void` | Removes agnostic rule(s) by start state. |
| `TransmutationAPI` | `removeEndStateFromReagent` | `reagent, end` | `void` | Removes reagent rule(s) by end state. |
| `TransmutationAPI` | `removeEndStateReagentAgnostic` | `end` | `void` | Removes agnostic rule(s) by end state. |
| `TransmutationAPI` | `clearMapOfReagent/clearReagentOfEntries` | `reagent` | `void` | Clears all rules for one reagent id. |
| `TransmutationAPI` | `clearReagentMap` | - | `void` | Clears all transmutation rules. |
| `TransmutationAPI` | `getRulesSnapshot` | - | `Map` | Unmodifiable snapshot for debug/admin. |

### State Format

- Block id only: `minecraft:stone`
- Blockstate: `minecraft:oak_log[axis=x]`
- Matching checks block + declared properties.

PSA:

- Optional `requiredSkill` only passes when that skill is enabled for the player.
- Runtime execution happens on server-side right-click block flow.

## Skill Change Tweaker (modern)

### Syntax

```js
SkillChangeAPI.addLevelUpCommands(String aptitude, int level, String... commands)
SkillChangeAPI.clearLevelUpCommands()
SkillChangeAPI.addSkillUnlockCommands(String skill, String... commands)
SkillChangeAPI.addSkillLockCommands(String skill, String... commands)
SkillChangeAPI.addPassiveLevelUpCommands(String passive, int level, String... commands)
SkillChangeAPI.addPassiveLevelDownCommands(String passive, int level, String... commands)
SkillChangeAPI.clearSkillUnlockCommands(String skill)
SkillChangeAPI.clearSkillLockCommands(String skill)
SkillChangeAPI.clearPassiveLevelUpCommands(String passive, int level)
SkillChangeAPI.clearPassiveLevelDownCommands(String passive, int level)
SkillChangeAPI.clearAllStateCommands()
```

### Blank Example

```js
SkillChangeAPI.addLevelUpCommands('alchemy_test', 2, 'tellraw {player} {"text":"Alchemy 2"}')
```

### Working Example

```js
SkillChangeAPI.clearLevelUpCommands()
SkillChangeAPI.clearAllStateCommands()
SkillChangeAPI.addLevelUpCommands(
  'smithing',
  8,
  'tellraw {player} {"text":"Smithing level 8 unlocked!","color":"gold"}',
  'give {player} minecraft:experience_bottle 4'
)
SkillChangeAPI.addSkillUnlockCommands(
  'one_handed',
  'tellraw {player} {"text":"Skill unlocked: {skill}","color":"green"}'
)
SkillChangeAPI.addPassiveLevelDownCommands(
  'luck',
  0,
  'tellraw {player} {"text":"Passive {passive} reset from {previous_level} to {new_level}","color":"red"}'
)
```

### Methods / Setters / Getters

| API | Method | Args | Return | Behavior / Constraints |
|---|---|---|---|---|
| `SkillChangeAPI` | `addLevelUpCommands` | `aptitude, level, commands...` | `void` | Registers commands for exact aptitude level. |
| `SkillChangeAPI` | `clearLevelUpCommands` | - | `void` | Clears all registered level-up commands. |
| `SkillChangeAPI` | `addSkillUnlockCommands` | `skill, commands...` | `void` | Executes when skill unlock state changes to unlocked. |
| `SkillChangeAPI` | `addSkillLockCommands` | `skill, commands...` | `void` | Executes when skill unlock state changes to locked. |
| `SkillChangeAPI` | `addPassiveLevelUpCommands` | `passive, level, commands...` | `void` | Executes when passive reaches that level by upgrade. |
| `SkillChangeAPI` | `addPassiveLevelDownCommands` | `passive, level, commands...` | `void` | Executes when passive reaches that level by downgrade/reset. |
| `SkillChangeAPI` | `clearSkillUnlockCommands` | `skill` | `void` | Clears skill unlock commands for one skill id. |
| `SkillChangeAPI` | `clearSkillLockCommands` | `skill` | `void` | Clears skill lock commands for one skill id. |
| `SkillChangeAPI` | `clearPassiveLevelUpCommands` | `passive, level` | `void` | Clears passive-up commands for one level key. |
| `SkillChangeAPI` | `clearPassiveLevelDownCommands` | `passive, level` | `void` | Clears passive-down commands for one level key. |
| `SkillChangeAPI` | `clearAllStateCommands` | - | `void` | Clears skill/passive state command registries. |
| `SkillChangeAPI` | `handleLevelUp` | `player, aptitude, prevLevel, newLevel` | `void` | Advanced/internal executor called by level-up packet flow. |
| `SkillChangeAPI` | `handleSkillUnlockStateChange` | `player, skill, prevUnlocked, newUnlocked` | `void` | Advanced/internal hook used by packet/API flows. |
| `SkillChangeAPI` | `handlePassiveLevelChanged` | `player, passive, prevLevel, newLevel` | `void` | Advanced/internal hook used by packet/API flows. |

PSA:

- Placeholder `{player}` is replaced with player name.
- Supported placeholders: `{player}`, `{aptitude}`, `{skill}`, `{passive}`, `{previous_level}`, `{new_level}`.
- Commands are executed by server command source stack.

## Player Data API (admin/runtime)

### Syntax

```js
PlayerDataAPI.<method>(player, ...)
```

### Blank Example

```js
PlayerDataAPI.setAptitudeLevel(player, 'strength', 10)
PlayerDataAPI.setSkillToggle(player, 'one_handed', true)
PlayerDataAPI.setPassiveLevel(player, 'luck', 2)
PlayerDataAPI.setTitleUnlocked(player, 'archmage_test', true)
```

### Working Example

```js
PlayerEvents.loggedIn(event => {
  var p = event.player

  PlayerDataAPI.setAptitudeLevel(p, 'strength', 12)
  var points = PlayerDataAPI.getAptitudePoints(p, 'strength')

  if (points >= 2) {
    PlayerDataAPI.unlockSkill(p, 'one_handed')
    PlayerDataAPI.setSkillToggle(p, 'one_handed', true)
  }

  PlayerDataAPI.setPassiveLevel(p, 'attack_damage', 1)

  if (PlayerDataAPI.hasTitleUnlocked(p, 'veteran_test_default')) {
    PlayerDataAPI.setPlayerTitle(p, 'veteran_test_default')
  }
})
```

### Methods / Setters / Getters (Aptitudes)

| Method | Args | Return | Validation / Clamp | Side effects |
|---|---|---|---|---|
| `getAptitudeLevel` | `player, aptitudeName` | `int` | `0` on invalid player/cap/name. | None. |
| `setAptitudeLevel` | `player, aptitudeName, level` | `void` | Clamp to `1..aptitudeCap`; upward changes respect `LevelLockAPI`. | Sync capability packet when changed. |
| `addAptitudeLevel` | `player, aptitudeName, levels` | `void` | No-op if `levels==0`; positive deltas respect `LevelLockAPI`. | Sync capability packet when changed. |
| `getGlobalLevel` | `player` | `int` | `0` if invalid. | None. |
| `getAptitudePoints` | `player, aptitudeName` | `int` | `0` on invalid input. | None. |
| `getAptitudePointsSpent` | `player, aptitudeName` | `int` | `0` on invalid input. | None. |
| `respecAptitude` | `player, aptitudeName` | `boolean` | `false` on invalid input. | Resets passive/skill state for that aptitude + sync + skill/passive change hooks. |

### Methods / Setters / Getters (Skills)

| Method | Args | Return | Validation / Clamp | Side effects |
|---|---|---|---|---|
| `getSkillToggle` | `player, skillName` | `boolean` | `false` on invalid input. | None. |
| `isSkillUnlocked` | `player, skillName` | `boolean` | `false` on invalid input. | None. |
| `unlockSkill` | `player, skillName` | `boolean` | Applies aptitude requirement and SP cost checks. | Sync + skill unlock/lock hooks when state changes. |
| `setSkillUnlocked` | `player, skillName, unlocked` | `void` | Directly sets unlocked state. | Sync + skill unlock/lock hooks when state changes. |
| `setSkillToggle` | `player, skillName, toggle` | `void` | If enabling and requirements fail, toggle forced to `false`. | Forces sync. |

### Methods / Setters / Getters (Passives)

| Method | Args | Return | Validation / Clamp | Side effects |
|---|---|---|---|---|
| `getPassiveLevel` | `player, passiveName` | `int` | `0` on invalid input. | None. |
| `setPassiveLevel` | `player, passiveName, level` | `void` | Clamp `0..passiveMax`. Upward changes still respect aptitude requirements. | Passive up/down hooks + sync when level changes. |

### Methods / Setters / Getters (Titles)

| Method | Args | Return | Validation / Clamp | Side effects |
|---|---|---|---|---|
| `hasTitleUnlocked` | `player, titleName` | `boolean` | `false` on invalid input. | None. |
| `setTitleUnlocked` | `player, titleName, unlocked` | `boolean` | `false` only on invalid input/title/cap. | Sync when state changed; may clear active title when locking current title. |
| `getPlayerTitle` | `player` | `String` | Empty string on invalid input. | None. |
| `setPlayerTitle` | `player, titleName` | `boolean` | Strict: fails if title is not unlocked. | Updates displayed name + sync on success. |
| `clearPlayerTitle` | `player` | `void` | No-op if already `titleless` or invalid. | Updates displayed name + sync when changed. |

PSA:

- These methods are admin/runtime controls. They can bypass or differ from normal GUI flow.
- Passive GUI downgrade is removed, but `setPassiveLevel` can still reduce levels.

## Events

Event group: `JLForkEvents`

- Client event: `JLForkEvents.aptitudeLevelUp`
- Server event: `JLForkEvents.aptitudeLevelUpServer`

### Syntax

```js
JLForkEvents.aptitudeLevelUp(event => {
  // client-side hook
})

JLForkEvents.aptitudeLevelUpServer(event => {
  // server-side hook
})
```

### Blank Example

```js
JLForkEvents.aptitudeLevelUpServer(event => {
  // do something
})
```

### Working Example

```js
JLForkEvents.aptitudeLevelUpServer(event => {
  var player = event.getPlayer()
  var aptitude = event.getAptitude()
  var prev = event.getPreviousLevel()
  var next = event.getNewLevel()

  if (aptitude.getName() == 'magic' && next > 20) {
    event.setCancelled(true)
    player.tell('Magic cap by script: 20')
  }
})
```

### Methods / Setters / Getters (LevelUpEvent)

| Method | Return | Notes |
|---|---|---|
| `getPlayer()` | `Player` | Player context. |
| `getAptitude()` | `Aptitude` | Aptitude being leveled. |
| `getPreviousLevel()` | `int` | Server event has real value; client event may use default placeholder. |
| `getNewLevel()` | `int` | Server event has real value; client event may use default placeholder. |
| `getCancelled()` | `boolean` | Cancellation state. |
| `setCancelled(boolean)` | `void` | Cancel upgrade flow for the event context. |

PSA:

- `aptitudeLevelUpServer` is the event to enforce server-side restrictions.
- Client event can improve UX but should not be your only gate.

## Full Scale Example

```js
// =========================
// File: kubejs/startup_scripts/jlfork_pack_setup.js
// =========================
// priority: 1000

if (!global.__jlfork_pack_setup_done) {
  global.__jlfork_pack_setup_done = true

  ;(() => {
    var JavaHashMap = Java.loadClass('java.util.HashMap')

    function reqMap(obj) {
      var m = new JavaHashMap()
      Object.keys(obj).forEach(k => m.put(String(k), obj[k]))
      return m
    }

    var smithing = Aptitude.addWithId(
      'my_pack:smithing',
      'minecraft:textures/block/stone_bricks.png',
      2,
      'kubejs:textures/item/example_item.png',
      'kubejs:textures/item/example_item.png',
      'kubejs:textures/item/example_item.png',
      'kubejs:textures/item/example_item.png'
    )
    smithing.setDisplayNameOverride('Smithing')
    smithing.setLevelCap(64)
    smithing.setBaseLevelCost(2)
    smithing.setSkillPointInterval(4)
    smithing.setLevelStaggering('1|1', '8|2', '16|3', '32|1', '48|0')
    smithing.setRankIcon(0, 'minecraft:textures/item/iron_ingot.png')
    smithing.setRankIcon(4, 'minecraft:textures/item/diamond.png')
    smithing.setEnabled(true)
    smithing.setHidden(false)

    var focus = Skill.addWithId(
      'my_pack:smithing_focus',
      'my_pack:smithing',
      4,
      'textures/skill/building/convergence.png',
      new Value(ValueType.MODIFIER, 1.25)
    )
    focus.setPointCost(2)

    var passive = Passive.addByAttributeId(
      'smithing_luck',
      'my_pack:smithing',
      'textures/skill/luck/passive_luck.png',
      'minecraft:generic.luck',
      '8b67a08f-9f1e-4f77-b5f1-6c2dc5b1d001',
      2.0,
      4, 8, 12, 16
    )
    passive.setPointCost(2)

    Title.add('smithing_master', false, false)
    TitleAPI.clearConditions('smithing_master')
    TitleAPI.conditions('smithing_master')
      .aptitude('my_pack:smithing', TitleComparator.GREATER_OR_EQUAL, 20)
      .register()

    LockItemAPI.addLock('minecraft:anvil', 'my_pack:smithing', 4)
    LockItemAPI.addModLock('minecraft', 'strength', 2)
    LevelLockAPI.addLevelLock('my_pack:smithing', 12, 'intelligence|8')
    VisibilityLockAPI.addVisibilityLock('my_pack:smithing', 'strength|4')

    NBTLockAPI.addItemNBTLock(
      'minecraft:book',
      '{MagicSeal:1b}',
      reqMap({ magic: 6, intelligence: 4 })
    )

    TransmutationAPI.setConsumeReagent(true)
    TransmutationAPI.addEntryToReagent(
      'minecraft:chorus_fruit',
      'minecraft:melon',
      'minecraft:pumpkin'
    )

    SkillChangeAPI.clearLevelUpCommands()
    SkillChangeAPI.clearAllStateCommands()
    SkillChangeAPI.addLevelUpCommands(
      'my_pack:smithing',
      8,
      'tellraw {player} {"text":"Smithing level 8 reached","color":"gold"}'
    )
  })()
}

// =========================
// File: kubejs/server_scripts/jlfork_pack_runtime.js
// =========================
// priority: 1000

JLForkEvents.aptitudeLevelUpServer(event => {
  var player = event.getPlayer()
  var aptitude = event.getAptitude()
  var next = event.getNewLevel()

  if (aptitude.getName() == 'smithing' && next > 40) {
    event.setCancelled(true)
    player.tell('Server rule: smithing max 40')
  }
})

PlayerEvents.loggedIn(event => {
  var p = event.player

  if (!PlayerDataAPI.hasTitleUnlocked(p, 'smithing_master')) {
    PlayerDataAPI.setTitleUnlocked(p, 'smithing_master', false)
  }

  var lvl = PlayerDataAPI.getAptitudeLevel(p, 'my_pack:smithing')
  if (lvl >= 20) {
    PlayerDataAPI.setTitleUnlocked(p, 'smithing_master', true)
  }
})
```

## Troubleshooting / Edge Cases

### Namespaces and IDs

- `add(...)` methods usually default to `justlevelingfork` namespace.
- Use `addWithId(...)` or `createNew*` wrappers for custom namespace.
- Always normalize to lower-case in scripts to avoid lookup mismatch.

### Java map vs JS object

- For `Map<?, ?>` overloads, Java `HashMap` is the safest option.
- JS object may work in many cases, but Java map avoids bridge edge cases.

### Startup reload behavior

- Registry changes belong in `startup_scripts`.
- `/kubejs reload_startup_scripts` is not always enough for deep registry changes.
- Best practice for stable tests: restart after startup registry edits.

### Invalid resource locations

- Texture or id parse failures throw exceptions at registration time.
- Validate every path: `namespace:path/to/file.png`.

### SNBT errors

- `NBTLockAPI` requires valid SNBT that parses into non-empty `CompoundTag`.
- If SNBT is invalid, registration throws `IllegalArgumentException`.

### Legacy requirement token format

- String requirements use only `aptitude|level` in this fork.
- Examples: `'strength|8'`, `'my_pack:smithing|12'`.
- Invalid tokens are ignored, so verify your final lock maps with snapshots.

### Level lock expectations

- `LevelLockAPI` gates target levels (not just "going from").
- Enforcement is server-side in packet/API/command flows.
- Decreasing levels is still allowed.

### Visibility lock expectations

- `VisibilityLockAPI` only hides aptitude UI entries.
- It does not auto-reset levels, skills, or passives.

### Transmutation state format

- Valid state examples: `minecraft:stone`, `minecraft:oak_log[axis=x]`.
- Invalid property names/values throw during registration.
- `requiredSkill` checks `skill.isEnabled(player)` at runtime.

### Skill toggle confusion

- `setSkillToggle(player, skill, true)` can silently become `false` if requirements are not met.
- Check `getSkillToggle` after setting to verify final state.

### Passive level set behavior

- `setPassiveLevel` clamp is `0..max`.
- Raising passive still respects aptitude requirements per level step.
- Lowering passive through API is allowed even if GUI downgrade is removed.

### Title conditions not applying

- Ensure `Title.add` was called for that title id.
- Ensure `TitleAPI.conditions(...).register()` was executed in startup.
- Verify condition variable names (`aptitude`, stat id, entity id) are valid.

### Localizations not showing

- Put lang files under `kubejs/assets/<namespace>/lang/`.
- Keys must match generated translation keys exactly.

---

If you want, the next iteration can add an appendix with copy-paste templates for each API section (`minimal`, `mid`, and `full`) so pack teams can bootstrap faster.
