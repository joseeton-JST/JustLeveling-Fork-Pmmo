# Addon Ownership (Addon-First)

## Goal
- `src/addon/**` is the only place for custom behavior and compatibility logic.
- `src/main/**` is treated as upstream reference and is not the release artifact for this repo.

## Runtime Target
- Base mod: `justlevelingfork-1.2.1`
- Addon mod: `justlevellingaddonjs`
- Strict compatibility policy: exact base version `1.2.1`

## Official Build/Run Flow
- Build addon only:
  - `./gradlew buildAddonOnly`
- Override base jar location:
  - `./gradlew buildAddonOnly -Pbase121JarPath="C:/path/to/justlevelingfork-1.2.1.jar"`
- Dev run profiles:
  - `./gradlew runAddonClient`
  - `./gradlew runAddonServer`

## Ownership Map
- API/Bindings integration:
  - `src/addon/java/com/joseetoon/justlevellingaddonjs/kubejs/**`
  - `src/addon/java/com/joseetoon/justlevellingaddonjs/integration/**`
- Binary compatibility bridge:
  - `src/addon/java/com/joseetoon/justlevellingaddonjs/compat/base121/Base121Bridge.java`
  - `src/addon/java/com/joseetoon/justlevellingaddonjs/compat/**`
- Compatibility/backport mixins:
  - `src/addon/java/com/joseetoon/justlevellingaddonjs/mixin/**`
  - `src/addon/resources/justlevellingaddonjs.mixins.json`
- ABI checks:
  - `checkAddonAgainstBase121Abi` task
  - Report: `build/tmp/addon_vs_base121_abi.csv`

## Verification Gates
- `buildAddonOnly` runs:
  - `checkMainMatchesOrigin`
  - `compileAddonJava`
  - `checkAddonAgainstBase121Abi`
  - `addonJar`
- Release artifact:
  - `build/libs/justlevellingaddonjs-<version>.jar`

