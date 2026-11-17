package me.jamino.wynnWanderer.pack;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Reads the coverage of each pixel of a PNG, the way Minecraft sees it: the alpha of images that have one,
 * the brightness of grayscale ones.
 * <p>
 * The Wynncraft pack has PNGs with wrong checksums, which Minecraft does not check but Java's ImageIO refuses,
 * so this decodes them itself and skips the checksums too.
 */
final class PngImage {
    private static final byte[] SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    final int width;
    final int height;
    /** Alpha, or brightness for images without alpha, from 0 to 255. */
    private final int[] coverage;

    private PngImage(int width, int height, int[] coverage) {
        this.width = width;
        this.height = height;
        this.coverage = coverage;
    }

    int coverage(int x, int y) {
        return coverage[y * width + x];
    }

    static PngImage read(byte[] data) {
        ByteBuffer in = ByteBuffer.wrap(data);
        for (byte b : SIGNATURE) {
            if (in.get() != b) throw new IllegalArgumentException("Not a PNG");
        }

        int width = 0, height = 0, bitDepth = 0, colorType = 0;
        byte[] palette = new byte[0];
        byte[] transparency = new byte[0];
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();

        while (in.remaining() >= 12) {
            int length = in.getInt();
            byte[] typeBytes = new byte[4];
            in.get(typeBytes);
            String type = new String(typeBytes, StandardCharsets.US_ASCII);
            byte[] chunk = new byte[length];
            in.get(chunk);
            in.getInt(); // CRC, not checked

            switch (type) {
                case "IHDR" -> {
                    ByteBuffer header = ByteBuffer.wrap(chunk);
                    width = header.getInt();
                    height = header.getInt();
                    bitDepth = header.get() & 0xFF;
                    colorType = header.get() & 0xFF;
                    header.get(); // compression
                    header.get(); // filter
                    if (header.get() != 0) throw new IllegalArgumentException("Interlaced PNGs are not supported");
                }
                case "PLTE" -> palette = chunk;
                case "tRNS" -> transparency = chunk;
                case "IDAT" -> compressed.writeBytes(chunk);
                default -> {
                }
            }
            if (type.equals("IEND")) break;
        }

        int channels = switch (colorType) {
            case 0, 3 -> 1;
            case 2 -> 3;
            case 4 -> 2;
            case 6 -> 4;
            default -> throw new IllegalArgumentException("Unknown PNG color type " + colorType);
        };
        int bitsPerPixel = channels * bitDepth;
        int bytesPerPixel = Math.max(1, bitsPerPixel / 8);
        int stride = (width * bitsPerPixel + 7) / 8;
        byte[] pixels = unfilter(inflate(compressed.toByteArray(), (stride + 1) * height), stride, height, bytesPerPixel);

        int[] coverage = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value;
                switch (colorType) {
                    case 0 -> value = scale(sample(pixels, y * stride, x, bitDepth), bitDepth);
                    case 3 -> {
                        int index = sample(pixels, y * stride, x, bitDepth);
                        value = index < transparency.length ? transparency[index] & 0xFF : 255;
                    }
                    // Minecraft cannot tell how much of an RGB pixel is covered, every pixel counts
                    case 2 -> value = 255;
                    default -> {
                        // The last channel is alpha, its high byte is enough
                        int offset = y * stride + x * bytesPerPixel + bytesPerPixel - (bitDepth / 8);
                        value = pixels[offset] & 0xFF;
                    }
                }
                coverage[y * width + x] = value;
            }
        }
        return new PngImage(width, height, coverage);
    }

    /** A sample of an image with one channel, which can be smaller than a byte. */
    private static int sample(byte[] pixels, int rowStart, int x, int bitDepth) {
        if (bitDepth >= 8) return pixels[rowStart + x * (bitDepth / 8)] & 0xFF;
        int bit = x * bitDepth;
        int b = pixels[rowStart + bit / 8] & 0xFF;
        return (b >> (8 - bitDepth - bit % 8)) & ((1 << bitDepth) - 1);
    }

    private static int scale(int value, int bitDepth) {
        return bitDepth >= 8 ? value : value * 255 / ((1 << bitDepth) - 1);
    }

    private static byte[] inflate(byte[] zlib, int size) {
        // Skips the zlib header and ignores the Adler-32 checksum at the end
        Inflater inflater = new Inflater(true);
        inflater.setInput(zlib, 2, zlib.length - 2);
        byte[] out = new byte[size];
        try {
            int read = 0;
            while (read < size && !inflater.finished()) {
                int n = inflater.inflate(out, read, size - read);
                if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) break;
                read += n;
            }
            if (read < size) throw new IllegalArgumentException("PNG image data ends early");
        } catch (DataFormatException e) {
            throw new IllegalArgumentException("Broken PNG image data", e);
        } finally {
            inflater.end();
        }
        return out;
    }

    private static byte[] unfilter(byte[] data, int stride, int height, int bpp) {
        byte[] out = new byte[stride * height];
        for (int y = 0; y < height; y++) {
            int filter = data[y * (stride + 1)] & 0xFF;
            int src = y * (stride + 1) + 1;
            int row = y * stride;
            for (int i = 0; i < stride; i++) {
                int raw = data[src + i] & 0xFF;
                int left = i >= bpp ? out[row + i - bpp] & 0xFF : 0;
                int up = y > 0 ? out[row - stride + i] & 0xFF : 0;
                int upLeft = y > 0 && i >= bpp ? out[row - stride + i - bpp] & 0xFF : 0;
                int value = switch (filter) {
                    case 0 -> raw;
                    case 1 -> raw + left;
                    case 2 -> raw + up;
                    case 3 -> raw + ((left + up) >> 1);
                    case 4 -> raw + paeth(left, up, upLeft);
                    default -> throw new IllegalArgumentException("Unknown PNG filter " + filter);
                };
                out[row + i] = (byte) value;
            }
        }
        return out;
    }

    private static int paeth(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) return a;
        return pb <= pc ? b : c;
    }
}
