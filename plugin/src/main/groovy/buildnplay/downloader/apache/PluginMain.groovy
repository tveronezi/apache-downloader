package buildnplay.downloader.apache

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = "download")
class PluginMain extends AbstractMojo {

    @Parameter(defaultValue = '${user.home}/.apache-downloader')
    String homeDir

    @Parameter(defaultValue = '${project.build.directory}/apache-downloaded-files')
    String copyToParentDir

    @Parameter
    List<ApacheResource> resources

    private def resourcesTool = new ResourcesTool();

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        def downloader = new ApacheDownloader(new File(homeDir))
        resources.each { ApacheResource resource ->
            def downloadedFile = downloader.download(resource.link)
            if (resource.copyTo) {
                resourcesTool.copy(downloadedFile, resource.copyTo)
            } else {
                resourcesTool.copy(downloadedFile, new File(copyToParentDir, downloadedFile.name).absolutePath)
            }
            if (resource.extractTo) {
                resourcesTool.unzip(downloadedFile, resource.extractTo)
            }
        }
    }

}
