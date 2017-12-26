package org.jenkinspipelinemagic

import org.jenkinspipelinemagic.Datetime
import org.jenkinspipelinemagic.Http

/**
 * Set a status on a GitHub SHA
 *
 * This works by invoking the GitHub status API:
 * https://developer.github.com/v3/repos/statuses/#create-a-status
 *
 * @param sha Commit id in git
 * @param sshUrl Full ssh url
 * @param description A short description of the status
 * @param context A string label to differentiate this status from the 
 *                status of other systems. Default: default
 * @param link The target URL to associate with this status. 
 *             This URL will be linked from the GitHub UI to 
 *             allow users to easily see the source of the 
 *             status. For example, if your continuous integration 
 *             system is posting build status, you would want to 
 *             provide the deep link for the build output for this 
 *             specific SHA: http://ci.example.com/user/repo/build/sha
 * @param state Required. The state of the status. Can be one of error, failure, pending, or success
 * @param gitHubApiKeyCredentialsId Optional. Name of Jenkins credential storing the access_token used for sending statuses to GitHub.
 */
def setShaStatus(sha, sshUrl, description, context="default", link=null, state="success", gitHubApiKeyCredentialsId="GITHUB_API_KEY") {
  def result
  def parts = describeGitUrlParts(sshUrl)
  def http = new Http()
  withCredentials([string(credentialsId: gitHubApiKeyCredentialsId, variable: 'access_token')]) {
    url = "https://api.github.com/repos/${parts["account"]}/${parts["repository"]}/statuses/${sha}?access_token=${access_token}"
    doc = [
      "target_url": link,
      "sha": sha,
      "ssh_url": sshUrl,
      "state": state,
      "description": description,
      "context": context
    ]
    result = http.restRequest(
      url,
      "POST",
      doc
    )
    if (result["status"] != 201) {
      throw new Exception("Failure to send status to GitHub. Url = ${url} SSH URL: ${sshUrl} ${result}")
    }
  }
}

/**
 * Execute work passed as closure within an envelope which will set 
 * a pending GitHub status before the work begins and then proceed
 * to update the GitHub check later with the success result.
 * 
 * Returns a long of the # of milliseconds that the logic executed in.
 * 
 * This works by invoking the GitHub status API:
 * https://developer.github.com/v3/repos/statuses/#create-a-status
 *
 * @param theClosure A Groovy closure that will be executed
 * @param sshUrl Full ssh url
 * @param sha Commit id in git
 * @param statusName Name of the GitHub status created
 * @param link The target URL to associate with this status. 
 *             This URL will be linked from the GitHub UI to 
 *             allow users to easily see the source of the 
 *             status. For example, if your continuous integration 
 *             system is posting build status, you would want to 
 *             provide the deep link for the build output for this 
 *             specific SHA: http://ci.example.com/user/repo/build/sha
 * @return Long Milliseconds duration of the routine
 */
def doClosureWithStatus(theClosure, sshUrl, sha, statusName, link) {
  def descriptionPrefix, millisDiff, duration, status
  def errToThrow
  def timeStart = new Datetime()
  def universalNodeName = 'slave'
  
  node(universalNodeName) {
    setShaStatus(
          sha, 
          sshUrl, 
          "Started at ${timeStart.formatCurrentTime()}", 
          statusName,
          link,
          "pending"
      )
  }
  long startMillis = System.currentTimeMillis()
  status = "success"
  try {
      theClosure()
  } catch (err) {
      errToThrow = err
      status = "failure"
  } finally {
      millisDiff = System.currentTimeMillis() - startMillis
      duration = hudson.Util.getTimeSpanString(millisDiff)
      descriptionPrefix = "Ran"
      if (status == "failure") {
          descriptionPrefix = "Failed"
      }
      node(universalNodeName) {
          setShaStatus(
              sha, 
              sshUrl, 
              "${descriptionPrefix} in ${duration}",
              statusName,
              link,
              status
          )
      }
  }
  if (status != "success") {
      println "doClosureWithStatus detected an exception. Throwing it now..."
      throw errToThrow
  }
  return millisDiff
}

/**
 * Return the parts of a giturl in an associative array.
 *
 * @param sshUrl Full ssh url
 * @return Return assoc array containing account and repo values
 */
def describeGitUrlParts(sshUrl) {
    def parts = parts = sshUrl.replaceFirst( /.*:(.*)\.git/, '$1' ).split('/')
    return [
        "account": parts[0],
        "repository": parts[1]
    ]
}

/**
 * Check the repo out into the current Jenkins workspace.
 *
 * This function internally caches the given git repository
 * into /ebs/projects to allow for high speed pulls after 
 * the repo is initially checked out.
 *
 * @param sshUrl Full ssh url
 * @param gitRef Commit ref. (Sha, tag, branch etc.)
 * @param destination Location of where the checked out repository should be unpacked to. 
 *					  If left null, the WORKSPACE directory is used.
 */
def fastCheckoutScm(sshUrl, gitRef, destination=null) {
  def parts, projectsDir
  if (!destination) {
    destination = "${WORKSPACE}"
  } else if (!destination.startsWith('/')) {
    destination = "${WORKSPACE}/${destination}"
  }
  parts = describeGitUrlParts(sshUrl)
  projectsDir = "/ebs/projects"
  sshagent(["GITHUB_MACHINE_USER_PRIVATE_KEY"]) {
    sh("""|#!/bin/bash
        |set -x
        |mkdir -p ${projectsDir}/${parts["account"]}
        |cd ${projectsDir}/${parts["account"]}
        |if [ ! -d "${parts["repository"]}" ]; then
        |    git clone --recurse-submodules ${sshUrl}
        |fi
        |if [ ! -d "${parts["repository"]}/.git" ]; then
        |    rm -rf ${parts["repository"]}
        |    git clone --recurse-submodules ${sshUrl}
        |fi
        |cd ${parts["repository"]}
        |if [ -f ".git/index.lock" ]; then
        | rm .git/index.lock
        |fi
        |git checkout master
        |git prune
        |git reset --hard origin/master
        |git pull origin master
        |git pull && git fetch --tags
        |cp -R ${projectsDir}/${parts["account"]}/${parts["repository"]}/. ${destination}
        |cd ${destination}
        |git checkout ${gitRef}""".stripMargin())  
  }
}
