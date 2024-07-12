import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

internal fun formatSizeInMb(sizeInMb: Float): String {
    return "%.1f Mb".format(sizeInMb)
}

internal fun File.sizeInMb(): Float {
    return Files.size(Paths.get(path)).toFloat() / 1_048_576
}