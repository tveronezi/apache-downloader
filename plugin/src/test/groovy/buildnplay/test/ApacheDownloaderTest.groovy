package buildnplay.test

import buildnplay.downloader.apache.ApacheDownloader
import buildnplay.downloader.apache.ApacheDownloaderException
import groovy.util.logging.Log
import org.junit.After
import org.junit.Assert
import org.junit.Test

@Log
class ApacheDownloaderTest extends ApacheDownloader {

    def generateMD5Impl = { File file ->
        super.generateMD5(file)
    }
    def originalGenerateMD5Impl = generateMD5Impl

    ApacheDownloaderTest() {
        super(new File(System.getProperty('buildDirectory'), 'downloaded'))
    }

    @After
    void revert() {
        generateMD5Impl = originalGenerateMD5Impl
    }

    @Override
    String generateMD5(File file) {
        return generateMD5Impl(file)
    }

    @Test
    void downloadTest() {
        def tomee = downloadHelper('bval/0.5/bval-parent-0.5-source-release.zip')
        Assert.assertEquals(
                tomee,
                downloadHelper('bval/0.5/bval-parent-0.5-source-release.zip')
        )
        Assert.assertEquals(
                tomee.lastModified(),
                downloadHelper('bval/0.5/bval-parent-0.5-source-release.zip').lastModified()
        )
        downloadHelper('commons/beanutils/binaries/commons-beanutils-1.9.2-bin.tar.gz')
    }

    @Test(expected = ApacheDownloaderException)
    void invalidDownloadTest() {
        download('i/dont/exist.tar.gz')
    }

    @Test(expected = ApacheDownloaderException)
    void invalidMd5Test() {
        generateMD5Impl = {
            "dummy"
        }
        download('commons/beanutils/binaries/commons-beanutils-1.9.2-bin.tar.gz')
    }

    File downloadHelper(String link) {
        def archive = download(link)
        Assert.assertTrue(archive.size() > 0)
        archive
    }
}
