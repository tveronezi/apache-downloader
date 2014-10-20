package buildnplay.downloader.apache

import groovy.util.logging.Log
import org.cyberneko.html.parsers.SAXParser

import java.security.MessageDigest
import java.util.logging.Level

@Log
class ApacheDownloader {

    private File home

    ApacheDownloader(File home) {
        this.home = home
    }

    String generateMD5(File file) {
        def digest = MessageDigest.getInstance('MD5')
        file.withInputStream() { is ->
            byte[] buffer = new byte[8192]
            int read = 0
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        def md5sum = digest.digest()
        def bigInt = new BigInteger(1, md5sum)
        bigInt.toString(16)
    }

    File download(String link) {
        def cache = new File(this.home, 'cache')
        def dest = new File(cache, link);
        if (dest.exists()) {
            log.fine("Apache resource already downloaded: $dest.absolutePath")
            return dest
        }
        def page = new XmlSlurper(new SAXParser()).parse("http://www.apache.org/dyn/closer.cgi/$link")
        def data = page.depthFirst().grep {
            it.name().equalsIgnoreCase('A') && it.@href.toString().endsWith(link)
        }.'@href'
        def found = data.find { url ->
            try {
                url.toString().toURL().withInputStream { InputStream is ->
                    dest.parentFile.mkdirs()
                    log.info("Downloading $url...")
                    dest.withOutputStream { OutputStream os ->
                        new BufferedOutputStream(os) << is
                    }
                }
                def md5 = "https://dist.apache.org/repos/dist/release/${link}.md5".toURL().text
                if (generateMD5(dest) == md5) {
                    true
                } else {
                    log.info("File in $url didn't pass the md5 test. Dropped.")
                    dest.delete()
                    false
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Impossible to download from ${url.toString()}", e)
            }
        }
        if (!found) {
            throw new ApacheDownloaderException("Apache resource not found. Resource: $link")
        }
        dest
    }

}
