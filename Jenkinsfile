#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: {
    Map<String, String> props = readProperties(file: 'repo.properties')
    def repoIncludeOrUpdate = [:]
    def customJobPrefix = [:]
    def customBranch = [:]
    props.each { key, value ->
      if(key.endsWith(".jenkinsjob")) {
        def index = key.lastIndexOf(".")
        if(index != -1) {
          def name = key.substring(0, index)
          customJobPrefix[name] = value
        }
      } else if(key.endsWith(".branch")) {
        def index = key.lastIndexOf(".")
        if(index != -1) {
          def name = key.substring(0, index)
          customBranch[name] = value
        }
      } else if(key.endsWith(".update")) {
        def index = key.lastIndexOf(".")
        if(index != -1) {
          def name = key.substring(0, index)
          repoIncludeOrUpdate[name] = repoIncludeOrUpdate[name] || value == 'true'
        }
      } else if(!key.endsWith(".dir") && !key.endsWith(".url")) {
        repoIncludeOrUpdate[key] = repoIncludeOrUpdate[key] || value == 'true'
      }
    }
    repoIncludeOrUpdate.findResults { name, include ->
      def jobPrefix = customJobPrefix[name] ?: "metaborg/$name"
      def branch = customBranch[name] ?: BRANCH_NAME
      include ? "$jobPrefix/$branch" : null
    }
  },

  preBuildCommand: './repo update',

  gradleParallel: false,
  gradleMaxWorkers: '1',
  gradleBuildTasks: 'buildAll archiveSpoofax3LwbEclipseInstallations',
  gradlePublishTasks: 'publishAll publishSpoofax3Lwb',
  gradleArgs: '--scan',

  buildMainBranch: false,
  buildTag: false,
  buildReleaseTag: false,

  publishDevelopBranch: true,
  publishReleaseTag: false,

  archive: true,
  archivePattern: 'spoofax.pie/lwb/spoofax.lwb.eclipse.repository/build/dist/Eclipse-*.zip',

  slack: true,
  slackChannel: "#spoofax3-dev",
)
