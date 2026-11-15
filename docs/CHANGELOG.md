# Changelog

## 0.1.1

### Changed

- Cloth Config and Mod Menu are now optional. WynnWanderer runs with just Fabric API and Wynntils
- Without Cloth Config, the settings come from `config/wynn-wanderer.toml`. Edit the file and restart the game to apply the changes
- With Cloth Config and Mod Menu installed, the config screen works the same as before and saves to the same file
- Mod Menu only shows the config button when Cloth Config is installed

### Notes

- Existing `wynn-wanderer.toml` files keep working, so you don't have to change anything
- If the config file has a line WynnWanderer can't read, that option uses its default, the problem gets logged, and the file is left as is so you can fix it
- New options get added to the config file automatically when you update

## 0.1.0

Initial release for Minecraft 1.21.11.

### Added

- Pill titles for all 24 major cities, including the Fruma ones, each in its own city color
- City titles mix colors and fonts the way Wynncraft does, with drop shadows tinted to match the title
- Support for custom title resource packs, see the [resource pack guide](RESOURCE_PACKS.md)
- Mod icon
- Tooltip for the "Enable Territory Titles" option

### Changed

- Updated to Minecraft 1.21.11 (Fabric Loader 0.19.5, Fabric API 0.141.6, Mod Menu 17.0.1, Cloth Config 21.11.153, Wynntils 4.2.13)
- Titles are drawn with the Fabric HUD element API
- Config changes apply as soon as you save them in the config screen
- Wynntils is now listed as a required dependency, so the game tells you if it's missing

### Development

- Switched from Yarn to the official Mojang mappings
- Updated to Loom 1.17.21 and Gradle 9.5.0, and added the Gradle wrapper
- Wynntils is downloaded from the Modrinth maven instead of a local jar
- Added unit tests (`gradlew test`) and an in-game smoke test (`gradlew runClientGameTest`)
