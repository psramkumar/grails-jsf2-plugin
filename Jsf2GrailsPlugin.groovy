import com.sun.faces.application.ApplicationFactoryImpl as Afact
import com.sun.faces.config.ConfigManager
import com.sun.faces.config.ConfigureListener
import com.sun.faces.config.WebConfiguration
import com.sun.faces.context.ExceptionHandlerFactoryImpl
import com.sun.faces.context.ExternalContextFactoryImpl
import com.sun.faces.context.FacesContextFactoryImpl as FCfact
import com.sun.faces.context.PartialViewContextFactoryImpl
import com.sun.faces.lifecycle.LifecycleFactoryImpl as Lfact
import com.sun.faces.renderkit.RenderKitFactoryImpl as RKfact
import grails.util.BuildSettingsHolder
import grails.util.Environment
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.compiler.GrailsClassLoader
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.codehaus.groovy.grails.web.pages.GroovyPage
import org.codehaus.groovy.grails.web.pages.TagLibraryLookup
import org.codehaus.groovy.grails.web.plugins.support.WebMetaUtils
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.doc4web.grails.jsf.RedirectDynamicMethod
import org.doc4web.grails.jsf.RenderDynamicMethod
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.validation.Errors
import org.springframework.web.context.request.RequestContextHolder as RCH

import javax.faces.FactoryFinder
import javax.faces.application.FacesMessage
import javax.faces.context.FacesContext
import javax.faces.event.PhaseListener
import javax.faces.lifecycle.Lifecycle
import javax.servlet.ServletContextEvent
import java.lang.reflect.Modifier

class Jsf2GrailsPlugin {
	// the plugin version
	private static final String JSF_SUFFIX = "Jsf"

	def version = "0.2-SNAPSHOT"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "2.2.0 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
			"grails-app/views/**",
			"web-app/**",
			"grails-app/controllers/**",
			"grails-app/conf/UrlMappings.groovy",
			"grails-app/conf/DataSource.groovy",
			"grails-app/domain/**"
	]

	def author = "Stephane MALDINI"
	def authorEmail = "smaldini@doc4web.com"
	def title = "JSF 2"
	def description = '''\\
h1. JSF-2 integration
* Bean By convention (no need for faces-config)
* Bean access to controller parameters (session,request,params...)
* Beans dynamic methods for navigation redirect(action?,bean?,view?,uri?,url?) and render(action?,view?,bean?),
* Automatic bean and service properties resolution
* 'void init()' called at bean initialization if present
* 'void dispose()' called at bean destruction if present
* JSF Extended request scope ('view')
* Access to web.xml configuration via Jsf2Config.groovy
* Access to faces-config generated in web-app/faces-config.xml
* Converters for Locale, Closure, Currency, Timezone
* i18n ready, fast access with #{m['key']}
* create-bean script
* Hibernate session managed from view rendering to view response
* Execute groovy code in EL expression ( #{gtag.groov[' def i = 2; 3.times{ i++ }; i; ']} )
* Support JSF2 Components - @ManagedBean ...

h2. Extra methods for beans :
* Support tagLibraries methods
* Support controllers properties
* redirect(action?,bean?,view?,uri?,url?),
* render(action?,view?,bean?)
* facesMessage(summary?, details?, severity?)

'''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/plugin/jsf2"

	def observe = ["services", "i18n"]

	def loadAfter = ["controllers", "services"]

	def config = loadJsf2Config()

	def watchedResources = [
			"file:./grails-app/controllers/**/*JsfController.groovy",
			"file:./plugins/*/grails-app/controllers/**/*JsfController.groovy"
	]

	def doWithWebDescriptor = { xml ->
		def facesconfig_location = getFacesconfigLocation(application)

		def params = [
				"javax.faces.application.CONFIG_FILES": facesconfig_location,
				"javax.faces.STATE_SAVING_METHOD": "server",
				"javax.faces.FACELETS_RESOURCE_RESOLVER": "org.doc4web.grails.jsf.facelets.GrailsResourceResolver",
				"javax.faces.PROJECT_STAGE": translateEnvironnement()
		]

		if (Environment.current.isReloadEnabled()) {
			params.put "javax.faces.REFRESH_PERIOD", "0"
		}



		ConfigObject confparams = config.jsf.params
		params = confparams.flatten() + params

		def servlets = xml.servlet[0]
		def servletsParams = [
				'Faces Servlet': ['javax.faces.webapp.FacesServlet', 1]
		]

		def mappings = xml.'servlet-mapping'[0]
		def mappingParams = [
				'Faces Servlet': "*.${config.jsf.extension}"
		]


		def contextparams = xml.'context-param'[0]
		contextparams + {
			params.each { key, value ->
				'context-param' {
					'param-name'(key)
					'param-value'(value)
				}
			}
		}

		servlets + {
			servletsParams.each { key, value ->
				servlet {
					'servlet-name'(key)
					'servlet-class'(value[0])
					'load-on-startup'(value[1])
				}
			}
		}

		def listedValues
		mappings + {
			mappingParams.each { key, value ->
				listedValues = [value]
				listedValues = listedValues.flatten()
				for (v in listedValues) {
					'servlet-mapping' {
						'servlet-name'(key)
						'url-pattern'(v)
					}
				}
			}
		}

		def mimeMapping = xml.'mime-mapping'
		mimeMapping + {
			'extension'('ico')
			'mime-type'('image/x-icon')
		}

	}

	def doWithSpring = {
		viewScope(org.doc4web.grails.jsf.faces.ViewScope)

		customScopeConfigurer(org.springframework.beans.factory.config.CustomScopeConfigurer) {
			scopes = ['view': ref('viewScope')]
		}

		gtag(org.doc4web.grails.jsf.TagJsfResolver) {
			gspTagLibraryLookup = ref('gspTagLibraryLookup')
		}

		for (beanClazz in application.controllerClasses) {
			if (!beanClazz.name.endsWith(JSF_SUFFIX)) {
				continue
			}

			GrailsClass beanClass = beanClazz

			String scope = beanClass.getPropertyValue("scope")
			def init = BeanUtils.findMethodWithMinimalParameters(beanClass.clazz, "init") ? true : false
			def dispose = BeanUtils.findMethodWithMinimalParameters(beanClass.clazz, "dispose") ? true : false

			"${beanClass.fullName}BeanClass"(MethodInvokingFactoryBean) {
				targetObject = ref("grailsApplication", true)
				targetMethod = "getArtefact"
				arguments = [ControllerArtefactHandler.TYPE, beanClass.fullName]
			}

			"${beanClass.propertyName}"(beanClass.getClazz()) { bean ->
				bean.autowire = true
				if (scope) {
					bean.scope = scope
				}
				if (init) {
					bean.initMethod = "init"
				}
				if (dispose) {
					bean.destroyMethod = "dispose"
				}
			}
		}

		if (manager.hasGrailsPlugin("hibernate")) {
			grailsHibernatePhaseListener(org.doc4web.grails.jsf.faces.GrailsHibernatePhaseListener) {
				sessionFactory = ref("sessionFactory")
			}
		}
	}

	def doWithDynamicMethods = { ctx ->

		GrailsPluginManager pluginManager = getManager()

		if (manager?.hasGrailsPlugin("groovyPages")) {
			TagLibraryLookup gspTagLibraryLookup = ctx.getBean("gspTagLibraryLookup") as TagLibraryLookup
			for (namespace in gspTagLibraryLookup.availableNamespaces) {
				def propName = GrailsClassUtils.getGetterName(namespace)
				def namespaceDispatcher = gspTagLibraryLookup.lookupNamespaceDispatcher(namespace)
				def beanClasses = application.controllerClasses*.clazz
				for (Class beanClass in beanClasses) {
					if (!beanClass.name.endsWith(JSF_SUFFIX)) {
						continue
					}
					MetaClass mc = beanClass.metaClass
					if (!mc.getMetaProperty(namespace)) {
						mc."$propName" = { namespaceDispatcher }
					}
					registerBeanMethodMissing(mc, gspTagLibraryLookup, ctx)
					Class superClass = beanClass.superclass
					// deal with abstract super classes
					while (superClass != Object.class) {
						if (Modifier.isAbstract(superClass.getModifiers())) {
							registerBeanMethodMissing(superClass.metaClass, gspTagLibraryLookup, ctx)
						}
						superClass = superClass.superclass
					}

				}
			}
		}

		for (bean in application.controllerClasses) {
			if (!bean.name.endsWith(JSF_SUFFIX)) {
				continue
			}

			MetaClass mc = bean.metaClass
			WebMetaUtils.registerCommonWebProperties(mc, application)
			registerBeanMethods(mc, ctx)
			mc.getPluginContextPath = {->
				pluginManager.getPluginPathForInstance(delegate) ?: ''
			}
		}
	}

	def doWithApplicationContext = { applicationContext ->
		this.initFacesContext()

		def servletContext = applicationContext.servletContext
		def event = new ServletContextEvent(servletContext)
		new ConfigureListener().contextInitialized(event)

		def lifecycleFactoryImpl = FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY)
		Lifecycle lifecycle = lifecycleFactoryImpl.getLifecycle(Lfact.DEFAULT_LIFECYCLE)

		if (manager.hasGrailsPlugin("hibernate")) {
			lifecycle.addPhaseListener applicationContext.getBean("grailsHibernatePhaseListener") as PhaseListener
		}
	}

	def onChange = { event ->

		if (event.source && event.source instanceof Class && application.isArtefactOfType(ControllerArtefactHandler.TYPE, event.source as Class)
				&& ((Class) event.source).name.endsWith(JSF_SUFFIX)) {
			def beanClass = application.addArtefact(ControllerArtefactHandler.TYPE, event.source as Class)
			def beanName = "${beanClass.propertyName}"
			def scope = beanClass.getPropertyValue("scope")

			def init = BeanUtils.findMethodWithMinimalParameters(beanClass.clazz, "init") ? true : false
			def dispose = BeanUtils.findMethodWithMinimalParameters(beanClass.clazz, "dispose") ? true : false

			def beans = beans {
				"$beanName"(beanClass.getClazz()) { bean ->
					bean.autowire = true
					if (scope) {
						bean.scope = scope
					}
					if (init) {
						bean.initMethod = "init"
					}
					if (dispose) {
						bean.destroyMethod = "dispose"
					}
				}
			}

			event.ctx.getBean('viewScope').timestamp = System.currentTimeMillis()

			beans.registerBeans(event.ctx)
			event.manager?.getGrailsPlugin("jsf2")?.doWithDynamicMethods(event.ctx)
		}
	}

	def onConfigChange = { event ->

	}

	private String getFacesconfigLocation(application) {
		if (application.warDeployed) {
			return '/WEB-INF/faces-config.xml'
		}
		else {
			return new File(BuildSettingsHolder.settings.baseDir.absolutePath + '/web-app/WEB-INF/faces-config.xml').absolutePath
		}
	}

	private ConfigObject loadJsf2Config() {

		def config = ConfigurationHolder.config
		def currentEnvName = Environment.getCurrent().name
		def slurper = new ConfigSlurper(currentEnvName)
		GroovyClassLoader classLoader = new GroovyClassLoader(getClass().classLoader)
		config.merge(slurper.parse(classLoader.loadClass('DefaultJsf2Config')))

		try {
			config.merge(slurper.parse(getClass().classLoader.loadClass('Jsf2Config')))
		}
		catch (Exception ignored) {
			// ignore, just use the defaults
		}

		ConfigurationHolder.setConfig config

		return config
	}

	private initFacesContext() {
		try {
			def oldcl = Thread.currentThread().getContextClassLoader()
			if (oldcl instanceof GrailsClassLoader) {
				Thread.currentThread().setContextClassLoader oldcl.parent
			}
			FactoryFinder.setFactory FactoryFinder.LIFECYCLE_FACTORY, Lfact.name
			FactoryFinder.setFactory FactoryFinder.APPLICATION_FACTORY, Afact.name
			FactoryFinder.setFactory FactoryFinder.RENDER_KIT_FACTORY, RKfact.name
			FactoryFinder.setFactory FactoryFinder.FACES_CONTEXT_FACTORY, FCfact.name

			FactoryFinder.setFactory FactoryFinder.EXCEPTION_HANDLER_FACTORY, ExceptionHandlerFactoryImpl.name
			FactoryFinder.setFactory FactoryFinder.EXTERNAL_CONTEXT_FACTORY, ExternalContextFactoryImpl.name
			FactoryFinder.setFactory FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY, PartialViewContextFactoryImpl.name

			// ConfigManager.getInstance().initialize(WebConfiguration.getInstance(context.getExternalContext()).getServletContext())
			def wc = WebConfiguration.getInstance FacesContext.currentInstance.externalContext
			ConfigManager.instance.initialize wc.servletContext
			/*println FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(ApplicationAssociate.ASSOCIATE_KEY)
			 Thread.currentThread().setContextClassLoader oldcl*/

		}
		catch (e) {
			e.printStackTrace()
		}
	}

	private String translateEnvironnement() {
		switch (Environment.current.name) {
			case Environment.DEVELOPMENT: return "development";
			case Environment.TEST: return "test";
			default:
				return "production";
		}
	}

	def registerBeanMethods(MetaClass mc, ApplicationContext ctx) {


		mc.el { String expr ->
			FacesContext fc = FacesContext.currentInstance
			return fc?.application?.evaluateExpressionGet(fc, expr, Object.class)
		}

		mc.facesMessage { Map args ->
			FacesContext.currentInstance.addMessage(args.id, new FacesMessage(args.summary, args.details, args.severity))
		}
		mc.facesMessage { String id, String msg ->
			FacesContext.currentInstance.addMessage(id, new FacesMessage(msg))
		}

		mc.getViewUri = { String name ->
			def webRequest = RCH.currentRequestAttributes()
			webRequest.attributes.getViewUri(name, webRequest.currentRequest)
		}
		mc.setErrors = { Errors errors ->
			RCH.currentRequestAttributes().setAttribute(GrailsApplicationAttributes.ERRORS, errors, 0)
		}
		mc.getErrors = {->
			RCH.currentRequestAttributes().getAttribute(GrailsApplicationAttributes.ERRORS, 0)
		}

		mc.hasErrors = {->
			errors?.hasErrors() ? true : false
		}

		def redirect = new RedirectDynamicMethod(ctx)

		mc.redirect = { Map args ->
			redirect.invoke(delegate, "redirect", args)
		}

		mc.redirect = { String arg ->
			redirect.invoke(delegate, "redirect", [action: arg])
		}

		def render = new RenderDynamicMethod(ctx)

		mc.render = { Map args ->
			render.invoke(delegate, "render", args)
		}

		mc.render = { String arg ->
			render.invoke(delegate, "render", [action: arg])
		}

	}

	static def registerBeanMethodMissing(MetaClass mc, TagLibraryLookup lookup, ApplicationContext ctx) {
		// allow controllers to call tag library methods
		mc.methodMissing = { String name, args ->
			args = args == null ? [] as Object[] : args
			def tagLibrary = lookup.lookupTagLibrary(GroovyPage.DEFAULT_NAMESPACE, name)
			if (tagLibrary) {
				MetaClass controllerMc = delegate.class.metaClass
				WebMetaUtils.registerMethodMissingForTags(controllerMc, lookup, GroovyPage.DEFAULT_NAMESPACE, name)
				if (controllerMc.respondsTo(delegate, name, args)) {
					return controllerMc.invokeMethod(delegate, name, args)
				}
				else {
					throw new MissingMethodException(name, delegate.class, args)
				}
			}
			else {
				throw new MissingMethodException(name, delegate.class, args)
			}
		}

	}

}
