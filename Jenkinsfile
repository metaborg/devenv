#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: [
    'metaborg/spoofax.pie/develop',
    'metaborg/sdf/spoofax3',
    'metaborg/stratego/spoofax3',
  ],

  buildMasterBranch: false,
  buildDevelopBranch: true,
  buildOtherBranch: false,
  buildTag: false,
  buildReleaseTag: false,

  publish: false,

  slack: false,
  slackChannel: "#pie-dev",
)
