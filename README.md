# WynnWanderer

<img width="468" height="200" alt="banner" src="https://github.com/user-attachments/assets/93f9c0cf-fada-47df-8b74-89195533a734" />

WynnWanderer is a Minecraft mod that pops up a title when you cross into a new territory on Wynncraft, with the full Wynncraft font treatment for all 24 major cities from Ragni to Hyloch. Made for my modpack [World of Wynncraft](https://modrinth.com/modpack/world-of-wynncraft), inspired by [Traveler's Titles](https://www.curseforge.com/minecraft/mc-mods/travelers-titles)

## What's Up

- Shows a title when you cross a territory border, using the territory data from Wynntils
- Pill titles for all **24 major cities**, including the Fruma ones, each in its own city color
- Everywhere else gets a plain "Entering ..." title, or turn that off and only get the cities
- Subtitles for the cities, which you can turn off
- Configurable text size, color and shadow
- Configurable position, so you can put the title wherever you want it on screen
- Configurable fade in, display, fade out and cooldown times
- Remembers the last few territories you visited so it doesn't spam you when you stand on a border
- Works with custom title resource packs, see the [resource pack guide](https://github.com/DrBiznes/wynn-wanderer/blob/master/docs/RESOURCE_PACKS.md) to make your own
- Configurable settings via Mod Menu or config file

## See It

Every city title in the mod:

<img width="1010" height="436" alt="gallery" src="https://github.com/user-attachments/assets/596c0ef8-cf17-4049-b56c-39fc697c20db" />

Colors come from the city, so Ragni is red and Corkus City is yellow like you'd expect!!

## Installation

1. Make sure you have Fabric Loader and Fabric API installed
2. Download and install Wynntils and Cloth Config
3. Download the latest version of WynnWanderer from the versions page
4. Place the downloaded .jar file in your Minecraft mods folder
5. Wander

## Configuration

You can configure WynnWanderer using Mod Menu. The position of the title and subtitle is set in the "Position Settings" section, and "Show Only Significant Territories" is the one to turn off if you want a title for every territory and not just the cities.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.5 or higher
- Fabric API
- Wynntils 4.2.13 or higher
- Cloth Config
- Mod Menu (optional, it's how you get to the config screen)

## Custom Titles

Want your own title art? WynnWanderer picks up resource packs that replace any city title with your own image. How to make one is in the [resource pack guide](https://github.com/DrBiznes/wynn-wanderer/blob/master/docs/RESOURCE_PACKS.md).

## Go Ham

- I don't know nothing about java so if you wanna fork this and fix this up go ham.
- Feel free to use this in your modpack
- It needs JDK 21 and the official Mojang mappings

```
./gradlew build              # builds the mod and runs the unit tests
./gradlew test               # unit tests only
./gradlew runClientGameTest  # in-game smoke tests
```

- **Unit tests** (`src/test`) cover the title logic that does not need the game: animation timing, territory tracking, title placement, title and color selection, and the language and mod metadata files.
- **Smoke tests** (`src/gametest`) start a real client with Wynntils installed and check that the mod loads, the config screen opens, and titles render where the config says. Screenshots are saved to `build/run/clientGameTest/screenshots`.
- The Wynntils version the mod is built against is set in `gradle.properties`.
- The banner and images above are made by `marketing/make_images.py`

## Acknowledgments

- Thanks to the Wynntils team, this mod would know nothing about where you are without them
- Thanks to the Traveler's Titles team for the idea
- Thanks to the Wynncraft team for creating an awesome MMORPG experience in Minecraft
- Thanks to shedaniel for Cloth Config

## Support and My Mods
Please report any bugs or feature suggestions on the Github Issues page, I'll be updating this frequently with community feedback and ideas! You can also [join my discord](https://discord.gg/jqFF64rXZZ) if you need direct support, or want to stay updated with all of my mods.
### Check out all my projects!
>   [World of Wynncraft Modpack](https://modrinth.com/modpack/world-of-wynncraft)

>   [WynnWanderer](https://modrinth.com/mod/wynnwanderer)

>   [WynnVista](https://modrinth.com/mod/wynnvista)

>   [Wynn Weapon Bigger](https://modrinth.com/mod/wynnweaponbigger)

>   [Nimble ReWynnded](https://modrinth.com/mod/nimble-rewynnded)

>   [Class Keybind Profiles](https://modrinth.com/mod/class-keybind-profiles)

>   [WynnBubbles](https://modrinth.com/mod/wynnbubbles)

>   [WynnLODGrabber](https://modrinth.com/mod/wynnlodgrabber)
