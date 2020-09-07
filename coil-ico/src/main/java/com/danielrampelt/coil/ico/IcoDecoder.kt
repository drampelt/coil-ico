package com.danielrampelt.coil.ico

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil.bitmap.BitmapPool
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.Options
import coil.size.PixelSize
import coil.size.Size
import okio.BufferedSource
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IcoDecoder : Decoder {
    companion object {
        private const val ICO_HEADER_SIZE = 6
        private const val ICO_ENTRY_SIZE = 16
    }

    override fun handles(source: BufferedSource, mimeType: String?): Boolean {
        val peek = source.peek()
        if (peek.readShortLe() != 0.toShort()) return false
        if (peek.readShortLe() != 1.toShort()) return false
        val numImages = peek.readShortLe()
        if (numImages <= 0 || numImages > 256) return false
        return true
    }

    override suspend fun decode(
        pool: BitmapPool,
        source: BufferedSource,
        size: Size,
        options: Options
    ): DecodeResult {
        val pixelSize = size as? PixelSize

        val peek = source.peek()
        peek.skip(4)
        val numImages = peek.readShortLe()

        var image = IconDirEntry.parse(peek)
        for (i in 1 until numImages) {
            // Choose image that best matches preferred size, or otherwise just largest image since max is only 256x256
            if (pixelSize != null && image.widthPixels >= pixelSize.width && image.heightPixels >= pixelSize.height) {
                break
            } else {
                val currentImage = IconDirEntry.parse(peek)
                if (currentImage.widthPixels * currentImage.heightPixels > image.widthPixels * image.heightPixels) {
                    image = currentImage
                }
            }
        }

        val imageBytes = ByteArray(image.size)
        source.skip(image.offset.toLong())
        source.read(imageBytes, 0, image.size)

        val decodeBytes = if (imageBytes.size > 4 &&
            imageBytes[0] == 0x89.toByte() &&
            imageBytes[1] == 0x50.toByte() &&
            imageBytes[2] == 0x4E.toByte() &&
            imageBytes[3] == 0x47.toByte()
        ) {
            // PNG data, pass directly to BitmapFactory
            imageBytes
        } else {
            // BMP data, create a new ICO with only the image we want in it
            ByteBuffer.wrap(ByteArray(ICO_HEADER_SIZE + ICO_ENTRY_SIZE + image.size)).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                putShort(0)
                putShort(1) // ICO format
                putShort(1) // 1 image
                put(image.width)
                put(image.height)
                put(image.numColors)
                put(0)
                putShort(image.colorPlanes)
                putShort(image.bytesPerPixel)
                putInt(image.size)
                putInt(ICO_HEADER_SIZE + ICO_ENTRY_SIZE)
                put(imageBytes)
            }.array()
        }

        val bitmap = BitmapFactory.decodeByteArray(decodeBytes, 0, decodeBytes.size, BitmapFactory.Options().apply {
            inPreferredConfig = options.config
        })
        val drawable = BitmapDrawable(options.context.resources, bitmap)
        return DecodeResult(drawable, false)
    }

    private data class IconDirEntry(
        val width: Byte,
        val height: Byte,
        val numColors: Byte,
        val colorPlanes: Short,
        val bytesPerPixel: Short,
        val size: Int,
        val offset: Int,
    ) {
        companion object {
            fun parse(source: BufferedSource): IconDirEntry {
                val width = source.readByte()
                val height = source.readByte()
                val numColors = source.readByte()
                source.skip(1)
                val colorPlanes = source.readShortLe()
                val bpp = source.readShortLe()
                val size = source.readIntLe()
                val offset = source.readIntLe()
                return IconDirEntry(width, height, numColors, colorPlanes, bpp, size, offset)
            }
        }

        val widthPixels: Int
            get() = width.toInt().takeUnless { it == 0 } ?: 256

        val heightPixels: Int
            get() = height.toInt().takeUnless { it == 0 } ?: 256
    }
}
