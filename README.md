# WynnWanderer

WynnWanderer is a Fabric mod for Wynncraft that displays territory titles when crossing borders, enhancing your exploration experience with elegant, non-intrusive notifications.

## Features

- Elegant territory title displays when crossing borders
- Configurable animation, position, and text styling
- Support for special styling of major cities and important locations
- Compatibility with Wynntils

## Creating Custom Title Resource Packs

WynnWanderer supports custom image-based titles through resource packs using Minecraft's font system.

### Basic Resource Pack Structure

```
your_resource_pack/
├── pack.mcmeta
└── assets/
    ├── wynn-wanderer/
    │   ├── lang/
    │   │   └── en_us.json
    │   └── textures/
    │       └── font/
    │           ├── ragni.png
    │           ├── detlas.png
    │           └── ... (more title images)
    └── minecraft/
        └── font/
            └── default.json
```

### Step 1: Create pack.mcmeta

```json
{
  "pack": {
    "pack_format": 34,
    "description": "WynnWanderer Visual Titles"
  }
}
```

### Step 2: Create Title Images

Create PNG images for each territory with transparent backgrounds. Recommended size is around 256x64 pixels, but you can adjust based on your preference.

### Step 3: Map Territories to Unicode Characters

In `assets/wynn-wanderer/lang/en_us.json`:

```json
{
  "wynn_wanderer.territory.ragni.title": "\uE001",
  "wynn_wanderer.territory.detlas.title": "\uE002",
  "wynn_wanderer.territory.almuj.title": "\uE003",
  "wynn_wanderer.territory.llevigar.title": "\uE004"
}
```

Replacing a title this way also replaces the pill it is drawn in by default, so nothing else has to be overridden. The image is tinted with the color of the territory, set it to white to keep the colors of the image:

```json
{
  "wynn_wanderer.territory.ragni.color": "ffffff"
}
```

#### Colors and fonts of the default titles

Each significant territory has three colors: `.color` (the pill), `.text_color` (the letters on the pill) and `.subtitle_color`. Titles and subtitles can use these codes on top of the regular formatting codes:

- `§r` switches to `.color`
- `§t` switches to `.text_color`, for text that is drawn on top of a background. Titles using it only get a shadow for their background.
- `§{namespace:font}` switches to a font, such as the `minecraft:banner/pill` font of the Wynncraft resource pack, and `§{}` back to the default font

### Step 4: Define Font Mappings

In `assets/minecraft/font/default.json`:

```json
{
  "providers": [
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/ragni.png",
      "ascent": 15,
      "height": 30,
      "chars": ["\uE001"]
    },
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/detlas.png",
      "ascent": 15,
      "height": 30,
      "chars": ["\uE002"]
    },
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/almuj.png",
      "ascent": 15,
      "height": 30,
      "chars": ["\uE003"]
    },
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/llevigar.png",
      "ascent": 15,
      "height": 30,
      "chars": ["\uE004"]
    }
  ]
}
```

### Font Definition Parameters

- **file**: Path to your image file
- **ascent**: Controls vertical positioning (higher values move the image up)
- **height**: Height of your image in pixels
- **chars**: Unicode character(s) to replace with this image

### Design Tips

- Use transparent backgrounds for seamless display
- Maintain consistent visual style across all title images
- Use Unicode points in the Private Use Area (E000-F8FF) to avoid conflicts
- Test with different screen resolutions
- You can create versions for different languages by using language-specific suffixes on filenames

### Important Territories

Consider creating images for these significant territories:

- Llevigar
- Detlas
- Ragni
- Almuj
- Cinfras
- Thesead
- Troms
- Eltom
- Olux
- Ahmsord
- Gelibord
- Rodoroc
- Corkus City
- Kandon-Beda
- Selchar
- Nemract
- Lutho
- Nesaak

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 1.21.11
2. Install Wynntils and Cloth Config
3. Place the WynnWanderer JAR in your mods folder
4. Launch Minecraft

## Configuration

Access the configuration screen through the Mod Menu interface when using Fabric. The position of the title and subtitle is set in the "Position Settings" section.

## Development

The mod uses the official Mojang mappings and needs JDK 21.

```
./gradlew build              # builds the mod and runs the unit tests
./gradlew test               # unit tests only
./gradlew runClientGameTest  # in-game smoke tests
```

- **Unit tests** (`src/test`) cover the title logic that does not need the game: animation timing, territory tracking, title placement, title and color selection, and the language and mod metadata files.
- **Smoke tests** (`src/gametest`) start a real client with Wynntils installed and check that the mod loads, the config screen opens, and titles render where the config says. Screenshots are saved to `build/run/clientGameTest/screenshots`.

The Wynntils version the mod is built against is set in `gradle.properties`.

## License

This project is licensed under the MIT License.