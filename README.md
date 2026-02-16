# JustLeveling Fork PMMO
`JustLeveling Fork PMMO` es una evolucion de `JustLeveling-Fork` para Forge 1.20.1, orientada a modpacks y servidores que necesitan un sistema de progresion mas configurable, mas scriptable y mas facil de mantener a largo plazo.

En lugar de ser solo una continuacion tecnica del fork base, este proyecto prioriza una identidad clara: convertir `aptitudes`, `skills`, `passives` y `titles` en una capa de gameplay que puedas disenar como parte del pack.

## Que hace diferente a este fork
- Enfoque pack-dev first: el sistema esta pensado para disenar progresion, no solo para usar configuracion estatica.
- Integracion KubeJS como superficie publica principal para contenido, reglas y control runtime.
- Filosofia `server-authoritative`: se refuerza consistencia entre GUI, logica de servidor, comandos y scripting.
- Modelo moderno sobre 1.20.1: conserva la estructura de JustLeveling, pero la adapta a necesidades actuales de packs.
- Documentacion tecnica orientada a uso real en packs, con estilo legado para facilitar migracion mental desde 1.12.2.

## En que se enfoca el mod
- Progresion por aptitudes con identidad propia.
- Skills y passives como desbloqueos de build.
- Titles como capa de meta-progresion visible.
- Locks y condiciones para controlar acceso a contenido.
- Automatizacion de reglas y eventos desde scripts.

## Para quien es este proyecto
- Pack-devs que necesitan control fino de progresion.
- Servidores con reglas RPG persistentes.
- Equipos que quieren iterar rapido sobre balance sin recompilar el mod en cada ajuste.

## Documentacion
- Guia principal de scripting y API:
  - [README_KUBEJS.md](README_KUBEJS.md)
- FAQ del fork base (referencia general):
  - [FAQ upstream](https://github.com/Senior-S/JustLeveling-Fork/wiki/FAQ)

## Instalacion
Dependencias requeridas:
- [ClothConfig](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
- [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios)
- [YetAnotherConfigLib](https://modrinth.com/mod/yacl/version/3.4.2+1.20.1-forge)

Dependencia adicional si usas BetterCombat:
- [PlayerAnimator](https://modrinth.com/mod/playeranimator)

## Enlaces
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/justleveling-fork)
- [Modrinth](https://modrinth.com/mod/justleveling-fork)
- [Discord](https://discord.gg/6c6cDU2mKj)

## Creditos
- `DPlayend` por el mod original.
- `Senior-S` por el fork base 1.20.1.
- Contribuidores de esta rama por la evolucion orientada a packs y KubeJS.
