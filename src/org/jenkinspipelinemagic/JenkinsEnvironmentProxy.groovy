package org.jenkinspipelinemagic

/**
 * This should have access to the real Jenkins pipeline
 * in flight. This is not useful though when we are 
 * running unit tests. Please do not call this function
 * directly. It is only useful when called from
 * JenkinsHelper.
 */
def getEnv(name) {
  return env[name]
}
