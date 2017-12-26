package org.jenkinspipelinemagic

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import org.jenkinspipelinemagic.JenkinsEnvironmentProxy
import hudson.slaves.OfflineCause
import hudson.util.RemotingDiagnostics
import org.apache.commons.lang3.StringUtils;

@Singleton
class JenkinsHelper implements Serializable {

  Map myEnv

  // --------------------------------------------------------------
  // This class implements the @Singleton decorator. You should not 
  // attempt to instantiate the class with the 'new' keyword and 
  // instead use the call pattern "JenkinsHelper.instance" to pull an
  // object and then to call this method to hydrate the Jenkins map
  // of environment variables.
  // --------------------------------------------------------------
  def setEnvironmentVariables(someIterator) {
      def theMap = [:]
      someIterator.each { name, value ->
          theMap[name.toString()] = value.toString()
      }
      this.myEnv = theMap
  }

  // ---------------------------------------------------------------
  // Return a URL to the Blue ocean version of a Jenkins job.
  // ---------------------------------------------------------------
  def getBlueOceanJobUrl(jobName=null, buildId=null, hostname=null) {
    if (!jobName) {
      jobName = getEnv('JOB_NAME')
    }
    if (!buildId) {
      buildId = getEnv("BUILD_ID")
    }
    if (!hostname) {
      hostname = Hudson.instance.getRootUrl()
    }
    return "https://${hostname}/blue/organizations/jenkins/${jobName}/detail/${jobName}/${buildId}/pipeline"
  }

  // -----------------------------------------------------------------
  // Set a global environment variable in the Jenkins master server.
  // *Please do not mess with this unless you know what you are doing.
  // -----------------------------------------------------------------
  def assignJenkinsGlobalVariable(name, value) {
    def instance = Jenkins.getInstance()
    def envVars = fetchEvars()
    envVars.put(name, value)
    instance.save()
  }

  // -----------------------------------------
  // Get a variable from the global env store.
  // -----------------------------------------
  def retrieveJenkinsGlobalVariable(name, defaultValue=null) {
    def envVars = fetchEvars()
    return envVars.get(name, defaultValue)
  }

  // -----------------------------------------
  // Get a variable from the global env store.
  // -----------------------------------------
  def retrieveJenkinsGlobalVariableNames() {
    def envVars = fetchEvars()
    return envVars.keySet() as List
  }

  def fetchEvars() {
    def instance = Jenkins.getInstance()
    def globalNodeProperties = instance.getGlobalNodeProperties()
    def envVarsNodePropertyList = globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)
    def newEnvVarsNodeProperty = null
    def envVars = null
    return envVarsNodePropertyList.get(0).getEnvVars()
  }

  // -------------------------------
  // Return an environment variable.
  // -------------------------------
  def getEnv(name) {
    if (name in myEnv) {
      return this.myEnv[name]
    } else {
      def e = new JenkinsEnvironmentProxy()
      return e.getEnv(name)
    }
  }
  
}
