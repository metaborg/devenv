#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: {
    Map<String, String> props = readProperties(file: 'repo.properties')
    def repoIncludeOrUpdate = [:]
    def jobPrefixes = [:]
    def jobBranches = [:]
    props.each { key, value ->
      if(key.endsWith(".jenkinsjob")) {
        def index = key.lastIndexOf(".")
        if(index != -1) {
          def name = key.substring(0, index)
          jobPrefixes[name] = value
        }
      } else if(key.endsWith(".branch")) {
        def index = key.lastIndexOf(".")
        if(index != -1) {
          def name = key.substring(0, index)
          jobBranches[name] = value
        }
      } else if(key.endsWith(".update")) {
        def index = key.lastIndexOf(".")
        if(index != -1) {
          def name = key.substring(0, index)
          repoIncludeOrUpdate[name] = repoIncludeOrUpdate[name] || value == 'true'
        }
      } else if(!key.endsWith(".dir") && !key.endsWith(".url")) {
        repoIncludeOrUpdate[key] = repoIncludeOrUpdate[key] || value == 'true'
        jobPrefixes[key] = "metaborg/$key"
      }
    }
    jobPrefixes.findResults { name, jobPrefix ->
      repoIncludeOrUpdate[name] ? "$jobPrefix/${jobBranches[name] ?: BRANCH_NAME}" : null
    }
  },

  preBuildTask: 'repoUpdate',

  buildMainBranch: false,
  buildTag: false,
  buildReleaseTag: false,
  publish: false,

  slack: false,
  slackChannel: "#pie-dev",
)
