# Creating Custom Title Resource Packs

[Back to the README](../README.md)

WynnWanderer supports custom image-based titles through resource packs using Minecraft's font system. The default city titles are made the same way, so the Wynncraft resource pack's pill font is what draws them.

## Basic Resource Pack Structure

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

## Step 1: Create pack.mcmeta

```json
{
  "pack": {
    "pack_format": 34,
    "description": "WynnWanderer Visual Titles"
  }
}
```

## Step 2: Create Title Images

Create PNG images for each territory with transparent backgrounds. Recommended size is around 256x64 pixels, but you can adjust based on your preference.

## Step 3: Map Territories to Unicode Characters

In `assets/wynn-wanderer/lang/en_us.json`:

```json
{
  "wynn_wanderer.territory.ragni.title": "",
  "wynn_wanderer.territory.detlas.title": "",
  "wynn_wanderer.territory.almuj.title": "",
  "wynn_wanderer.territory.llevigar.title": ""
}
```

Replacing a title this way also replaces the pill it is drawn in by default, so nothing else has to be overridden. The image is tinted with the color of the territory, set it to white to keep the colors of the image:

```json
{
  "wynn_wanderer.territory.ragni.color": "ffffff"
}
```

### Colors and fonts of the default titles

Each significant territory has three colors: `.color` (the pill), `.text_color` (the letters on the pill) and `.subtitle_color`. Titles and subtitles can use these codes on top of the regular formatting codes:

- `§r` switches to `.color`
- `§t` switches to `.text_color`, for text that is drawn on top of a background. Titles using it only get a shadow for their background.
- `§{namespace:font}` switches to a font, such as the `minecraft:banner/pill` font of the Wynncraft resource pack, and `§{}` back to the default font

## Step 4: Define Font Mappings

In `assets/minecraft/font/default.json`:

```json
{
  "providers": [
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/ragni.png",
      "ascent": 15,
      "height": 30,
      "chars": [""]
    },
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/detlas.png",
      "ascent": 15,
      "height": 30,
      "chars": [""]
    },
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/almuj.png",
      "ascent": 15,
      "height": 30,
      "chars": [""]
    },
    {
      "type": "bitmap",
      "file": "wynn-wanderer:textures/font/llevigar.png",
      "ascent": 15,
      "height": 30,
      "chars": [""]
    }
  ]
}
```

## Font Definition Parameters

- **file**: Path to your image file
- **ascent**: Controls vertical positioning (higher values move the image up)
- **height**: Height of your image in pixels
- **chars**: Unicode character(s) to replace with this image

## Design Tips

- Use transparent backgrounds for seamless display
- Maintain consistent visual style across all title images
- Use Unicode points in the Private Use Area (E000-F8FF) to avoid conflicts
- Test with different screen resolutions
- You can create versions for different languages by using language-specific suffixes on filenames

## Territories With Titles

These all have a title already, so these are the ones to replace:

- Llevigar, Gelibord, Olux, Rodoroc, Eltom, Cinfras, Ahmsord
- Kandon-Beda, Thesead, Corkus City, Selchar, Nemract, Almuj
- Ragni, Detlas, Lutho, Nesaak, Troms, Alekin
- Fruma: Fort Torann, Espren, Timasca, Aldwell, Hyloch
