import grails.util.BuildSettingsHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder

//http://jira.codehaus.org/browse/GRAILS-6012
//TODO workaround for web.xml
eventPackagingEnd = {

  def config = ConfigurationHolder.config

  if (config.jsf.copy.web.xml)
    Ant.copy(file: BuildSettingsHolder.settings.webXmlLocation , todir: "${basedir}/web-app/WEB-INF")
}