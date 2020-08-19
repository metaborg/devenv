#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: {
    // Go over the included and/or updated repositories in repo.properties to determine the upstream projects.
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
          repoIncludeOrUpdate[key] = value == 'true'
        }
      } else if(!key.endsWith(".dir") && !key.endsWith(".url") ) {
        repoIncludeOrUpdate[key] = value == 'true'
        jobPrefixes[key] = "metaborg/$key"
      }
    }
    jobPrefixes.findResult { name, jobPrefix ->
      if(repoIncludeOrUpdate[name]) {
        def branch = jobBranches[name] ?: BRANCH_NAME
        "$jobPrefix/$branch"
      } else {
        null
      }
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
