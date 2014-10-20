package base.jchat

def ant = new AntBuilder()
def target = new File('target')

def executeMvn(String srcDirPath) {
    def srcDir = new File(srcDirPath)
    def mvnHome = new File(target, 'bin/apache-maven-3.2.3')
    def mvnBin = new File(mvnHome, 'bin/mvn')
    mvnBin.executable = true
    def m2 = new File(target, 'm2')
    m2.mkdirs()

    def builder = new ProcessBuilder().inheritIO()
            .command("${mvnBin.absolutePath}", 'clean', 'install', '-DskipTests')
            .directory(srcDir)

    def env = builder.environment()
    env.put('JAVA_OPTS', '-Xms128m -Xmx256m')
    env.put('MAVEN_OPTS', "-Dmaven.repo.local=${m2.absolutePath}")
    env.put('M2_HOME', mvnHome.absolutePath)
    if (System.getenv('JAVA_HOME')) {
        env.put('JAVA_HOME', System.getenv('JAVA_HOME'))
    }
    def process = builder.start()
    process.waitFor()
    new File(srcDir, 'target')
}

def getWar(String targetName) {
    def war = new File(target, "${targetName}.war")
    if(!war.exists()) {
        def original = executeMvn(target, "src/master/${targetName}-master").listFiles().find { File file ->
            file.name.endsWith('.war')
        }
        ant.copy(file: original, tofile: war)
    }
    war

}
def jchatWar = getWar('jchat')
def springchatWar = getWar('springchat')
def tomeeDir = new File(target, 'temp/tomee')
ant.delete(dir: tomeeDir)
ant.unzip(src: new File(target, 'tomee.zip'), dest: tomeeDir)
ant.copy(file: jchatWar, todir: new File(tomeeDir, 'webapps'))
