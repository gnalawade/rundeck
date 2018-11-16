package rundeck.services

import com.dtolabs.rundeck.core.common.Framework
import com.dtolabs.rundeck.core.execution.service.TourLoaderPluginService
import com.dtolabs.rundeck.core.plugins.ConfiguredPlugin
import com.dtolabs.rundeck.core.plugins.ProviderIdent
import com.dtolabs.rundeck.core.plugins.configuration.PropertyResolver
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope
import com.dtolabs.rundeck.plugins.tours.TourLoaderPlugin
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class TourLoaderServiceSpec extends Specification implements ServiceUnitTest<TourLoaderService>{

    def setup() {
        service.frameworkService = Mock(FrameworkService)
        def framework = Stub(Framework)
        service.frameworkService.getRundeckFramework() >> framework
        framework.getTourLoaderService() >> Mock(TourLoaderPluginService)
        service.frameworkService.getFrameworkPropertyResolver(_,_) >> new PropertyResolver() {
            @Override
            Object resolvePropertyValue(final String name, final PropertyScope scope) {
                return null
            }
        }
    }

    def cleanup() {
    }

    def "list all tour manifests"() {
        setup:
        service.pluginService = Mock(PluginService)


        when:
        TestTourLoader tourLoader = new TestTourLoader()
        TestTourLoader tourLoader2 = new TestTourLoader()
        1 * service.frameworkService.getRundeckFramework().getTourLoaderService().listProviders() >> [new ProviderIdent("tourLoader","tourLoader"), new ProviderIdent("tourLoader2","tourLoader2") ]
        2 * service.pluginService.configurePlugin(_,_,_,_) >> new ConfiguredPlugin<TestTourLoader>(tourLoader,null)
        def allManifests = service.listAllTourManifests()

        then:
        allManifests.size() == 2
        allManifests.find { it.provider == "tourLoader"}
        allManifests.find { it.provider == "tourLoader2"}

    }

    def "list tours"() {
        setup:
        service.pluginService = Mock(PluginService)

        when:
        1 * service.pluginService.configurePlugin(_,_,_,_) >> new ConfiguredPlugin<TestTourLoader>(new TestTourLoader(),null)
        def tourList = service.listTours("testloader")

        then:
        tourList.tours.size() == 1

    }

    def "get tours"() {
        setup:
        service.pluginService = Mock(PluginService)

        when:
        1 * service.pluginService.configurePlugin(_,_,_,_) >> new ConfiguredPlugin<TestTourLoader>(new TestTourLoader(),null)
        def tour = service.getTour("testloader","tour1.json")

        then:
        tour != null

    }

    class TestTourLoader implements TourLoaderPlugin {

        @Override
        Map getTourManifest() {
            return [tours:[
                    [key:"tour1",name:"Tour 1"]
            ]]
        }

        @Override
        Map getTour(final String tourId) {
            return [
                    key:"tour1",
                    name:"Tour 1",
                    steps: [
                            [title:"First Step",content:"My First Step"]
                    ]
            ]
        }
    }

}
