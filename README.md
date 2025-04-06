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

1. Install Fabric Loader and Fabric API
2. Install Wynntils
3. Place the WynnWanderer JAR in your mods folder
4. Launch Minecraft

## Configuration

Access the configuration screen through the Mod Menu interface when using Fabric.

## License

This project is licensed under the MIT License.