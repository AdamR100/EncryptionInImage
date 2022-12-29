package cryptography

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

fun main() {
    println("Task (hide, show, exit):")
    when (readln()) {
        "hide" -> hide()
        "show" -> show()
        "exit" -> {
            print("Bye!")
            return
        }
    }
    main()
}

fun show() {
    println("Input image file:")
    val inputImageFile = File(readln())
    println("Password:")
    val password = readln().encodeToByteArray()
    if (!inputImageFile.exists()) {
        println("Can't read input file!")
        return
    }
    var bytes = byteArrayOf()
    val inputImage = ImageIO.read(inputImageFile)
    pixelIterate@ for (y in 0 until inputImage.height) {
        for (x in 0 until inputImage.width) {
            val byte = (x) * (y + 1)
            val bit = Color(inputImage.getRGB(x, y)).blue shl 31 ushr 31
            if (byte % 8 == 0) {
                bytes += (bit shl 7).toByte()
            } else {
                bytes[byte / 8] = (bytes[byte / 8].toInt() or (bit shl (7 - byte % 8))).toByte()
            }
            try {
                if (bytes.last() == 3.toByte() && bytes[bytes.lastIndex - 1] == 0.toByte() &&
                    bytes[bytes.lastIndex - 2] == 0.toByte()
                ) {
                    break@pixelIterate
                }
            } catch (outOfBounds: ArrayIndexOutOfBoundsException) {
                continue
            }
        }
    }
    val array = ByteArray(bytes.size - 3)
    for (i in 0 until bytes.lastIndex - 2) {
        array[i] = (bytes[i].toInt() xor password[i % password.size].toInt()).toByte()
    }
    println("Message:")
    println(array.toString(Charsets.UTF_8))
}

fun hide() {
    println("Input image file:")
    val input = readln()
    val inputFile = File(input)
    println("Output image file:")
    val output = readln()
    val outputFile = File(output)
    if (!inputFile.exists()) {
        println("Can't read input file!")
        return
    }
    println("Message to hide:")
    val clearMessage = readln().encodeToByteArray()
    println("Password:")
    val password = readln().encodeToByteArray()
    var message = clearMessage
    for (i in 0..clearMessage.lastIndex) {
        message[i] = (clearMessage[i].toInt() xor password[i % password.size].toInt()).toByte()
    }
    val image = ImageIO.read(inputFile)
    if ((message.lastIndex + 1) * 8 > image.width * image.height) {
        println("The input image is not large enough to hold this message.")
        return
    }
    message += byteArrayOf(0, 0, 3)
    pixelLoop@ for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val byte = (x) * (y + 1)
            val bit = message[byte / 8].toInt() shl ((byte % 8) + 24) ushr ((byte % 8) + 24) ushr (7 - byte % 8)
            //adequate bit for each pixel
            if (bit == 0) {
                image.setRGB(
                    x, y, Color(
                        Color(image.getRGB(x, y)).red,
                        Color(image.getRGB(x, y)).green, Color(image.getRGB(x, y)).blue shr 1 shl 1
                    ).rgb
                )
            } else if (bit != 1) {
                print(bit)
                throw Exception("Bit != 1 :(")
            } else {
                image.setRGB(
                    x, y, Color(
                        Color(image.getRGB(x, y)).red,
                        Color(image.getRGB(x, y)).green, Color(image.getRGB(x, y)).blue or 1
                    ).rgb
                )
            }
            if (message.size * 8 == byte + 1) break@pixelLoop
        }
    }
    ImageIO.write(image, "png", outputFile)
    println("Message saved in $output image.")
}