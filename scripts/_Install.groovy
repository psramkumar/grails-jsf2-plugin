import groovy.text.SimpleTemplateEngine

//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
//
//    ant.mkdir(dir:"${basedir}/grails-app/jobs")
//


if (!new File("${basedir}/grails-app/conf/Jsf2Config.groovy").exists()) {

  def engine = new SimpleTemplateEngine()
  def configbinding = [:]
  def configtemplate = engine.createTemplate(new FileReader("${jsf2PluginDir}/src/templates/conf/Jsf2Config.groovy")).make(configbinding)

  new File("${basedir}/grails-app/conf/Jsf2Config.groovy").write(configtemplate.toString())
} else {
  println "Existing Jsf2Config.groovy located.  Leave it alone !"
}
if (!new File("${basedir}/web-app/WEB-INF/faces-config.xml").exists()) {
  Ant.copy(file:"${jsf2PluginDir}/src/templates/faces-config.xml",todir:"${basedir}/web-app/WEB-INF")
}else {
  println "Existing Faces-config.xml located. Leave it alone !"
}
if (!new File("${basedir}/grails-app/views/layouts/page-template.xhtml").exists()) {
  Ant.copy(file:"${jsf2PluginDir}/src/templates/page-template.xhtml",todir:"${basedir}/grails-app/views/layouts")
}else {
  println "Existing page-template located. Leave it alone !"
}
if (new File("${basedir}/web-app/css/main.css").exists()) {
  Ant.copy(file:"${basedir}/web-app/css/main.css",todir:"${basedir}/web-app/resources/css/")
}
if (new File("${basedir}/web-app/images/favicon.ico").exists()) {
  Ant.copy(file:"${basedir}/web-app/images/favicon.ico",todir:"${basedir}/web-app/resources/images/")
}
if (new File("{basedir}/web-app/images/grails_logo.jpg").exists()) {
  Ant.copy(file:"${basedir}/web-app/images/grails_logo.jpg",todir:"${basedir}/web-app/resources/images/")
}
if (new File("${basedir}/web-app/images/grails_logo.png").exists()) {
  Ant.copy(file:"${basedir}/web-app/images/grails_logo.png",todir:"${basedir}/web-app/resources/images/")
}
Ant.copy(file:"${jsf2PluginDir}/src/templates/ice-fade-bkgnd.gif",todir:"${basedir}/web-app/resources/images/")
