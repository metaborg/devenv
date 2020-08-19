#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: {
    Map<String, String> props = readProperties(file: 'repo.properties')
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
      } else if(!key.endsWith(".update") && !key.endsWith(".dir") && !key.endsWith(".url")) {
        jobPrefixes[key] = "metaborg/$key"
      }
    }
    jobPrefixes.collect { name, jobPrefix ->
      def branch = jobBranches[name] ?: BRANCH_NAME
      "$jobPrefix/$branch"
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
