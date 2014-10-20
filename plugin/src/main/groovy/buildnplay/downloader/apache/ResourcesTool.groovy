package buildnplay.downloader.apache

import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.utils.IOUtils

class ResourcesTool {

    private def ant = new AntBuilder()

    // TODO: replace it by the ant version once it is available
    // https://issues.apache.org/bugzilla/show_bug.cgi?id=56641
    private static void unTar(File inputFile, File outputDir) {
        inputFile.withInputStream { inputStream ->
            outputDir.mkdirs()
            def tarArchiveInputStream = new ArchiveStreamFactory().createArchiveInputStream('tar', inputStream)
            TarArchiveEntry entry
            while ((entry = tarArchiveInputStream.getNextEntry() as TarArchiveEntry) != null) {
                def outputFile = new File(outputDir, entry.getName())
                outputFile.parentFile.mkdirs()
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        if (!outputFile.mkdirs()) {
                            throw new IllegalStateException("Couldn't create directory $outputFile.absolutePath.")
                        }
                    }
                } else {
                    outputFile.withOutputStream { outStream ->
                        IOUtils.copy(tarArchiveInputStream, outStream);
                    }
                }
            }
        }
    }

    void unzip(File contentFile, String unzipTo) {
        if (contentFile.name.toLowerCase().endsWith('.zip')) {
            ant.unzip(
                    src: contentFile,
                    dest: new File(unzipTo),
                    overwrite: 'false'
            )
        } else if (contentFile.name.toLowerCase().endsWith('.tar.gz')) {
            def dest = new File(unzipTo);
            def temp = new File(dest.parentFile, 'temp')
            temp.mkdirs()
            ant.gunzip(
                    src: contentFile,
                    dest: temp
            )
            unTar(temp.listFiles().first(), dest)
            ant.delete(dir: temp)
        }
    }

    void copy(File contentFile, String copyTo) {
        def toFile = new File(copyTo)
        toFile.parentFile.mkdirs()
        ant.copy(
                file: contentFile,
                tofile: toFile
        )
    }
}
