/*
 * Copyright 2006-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Gant script that creates a Grails Quartz job
 *
 * @author Stephane Maldini
 *
 * @since 0.2
 */

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"



if (grails.util.GrailsUtil.grailsVersion.startsWith("1.0")) {
  includeTargets << new File("${grailsHome}/scripts/Init.groovy")
  includeTargets << new File("${grailsHome}/scripts/CreateIntegrationTest.groovy")

  target('default': "Creates a new JavaServerFaces Bean") {
    depends(checkVersion)

    typeName = "Bean"
    artifactName = "Bean"
    artifactPath = "grails-app/beans"

    createArtifact()
    createTestSuite()
  }
}
else {
  includeTargets << grailsScript("_GrailsInit")
  includeTargets << grailsScript("_GrailsCreateArtifacts")

  target('default': "Creates a new JavaServerFaces Bean") {
    depends(checkVersion, parseArguments)

    def type = "Bean"
    promptForName(type: type)

    def name = argsMap["params"][0]
    createArtifact(name: name, suffix: type, type: type, path: "grails-app/beans")
    createUnitTest(name: name, suffix: type)
  }
}
