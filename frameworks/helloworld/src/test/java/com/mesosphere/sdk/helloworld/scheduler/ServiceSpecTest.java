package com.mesosphere.sdk.helloworld.scheduler;

import com.mesosphere.sdk.dcos.Capabilities;
import org.apache.curator.test.TestingServer;
import org.apache.mesos.SchedulerDriver;
import com.mesosphere.sdk.scheduler.DefaultScheduler;
import com.mesosphere.sdk.specification.DefaultServiceSpec;
import com.mesosphere.sdk.specification.yaml.YAMLServiceSpecFactory;
import com.mesosphere.sdk.state.StateStoreCache;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Collections;

import static com.mesosphere.sdk.specification.yaml.YAMLServiceSpecFactory.generateRawSpecFromYAML;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceSpecTest {

    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock
    private SchedulerDriver mockSchedulerDriver;

    @BeforeClass
    public static void beforeAll() {
        environmentVariables.set("EXECUTOR_URI", "");
        environmentVariables.set("LIBMESOS_URI", "");
        environmentVariables.set("PORT0", "8080");

        environmentVariables.set("SLEEP_DURATION", "1000");
        environmentVariables.set("HELLO_COUNT", "2");
        environmentVariables.set("HELLO_PORT", "4444");
        environmentVariables.set("HELLO_VIP_NAME", "helloworld");
        environmentVariables.set("HELLO_VIP_PORT", "9999");
        environmentVariables.set("HELLO_CPUS", "0.1");
        environmentVariables.set("HELLO_MEM", "512");
        environmentVariables.set("HELLO_DISK", "5000");

        environmentVariables.set("WORLD_COUNT", "3");
        environmentVariables.set("WORLD_CPUS", "0.2");
        environmentVariables.set("WORLD_MEM", "1024");
        environmentVariables.set("WORLD_FAILS", "3");
        environmentVariables.set("WORLD_DISK", "5000");
    }

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testYmlBase() throws Exception {
        deserializeServiceSpec("svc.yml");
        validateServiceSpec("svc.yml");
    }

    @Test
    public void testYmlSimple() throws Exception {
        deserializeServiceSpec("examples/simple.yml");
        validateServiceSpec("examples/simple.yml");
    }

    @Test
    public void testYmlPlan() throws Exception {
        deserializeServiceSpec("examples/plan.yml");
        validateServiceSpec("examples/plan.yml");
    }

    @Test
    public void testYmlSidecar() throws Exception {
        deserializeServiceSpec("examples/sidecar.yml");
        validateServiceSpec("examples/sidecar.yml");
    }

    @Test
    public void testYmlTaskcfg() throws Exception {
        deserializeServiceSpec("examples/taskcfg.yml");
        validateServiceSpec("examples/taskcfg.yml");
    }

    @Test
    public void testYmlUri() throws Exception {
        deserializeServiceSpec("examples/uri.yml");
        validateServiceSpec("examples/uri.yml");
    }

    @Test
    public void testYmlWebUrl() throws Exception {
        deserializeServiceSpec("examples/web-url.yml");
        validateServiceSpec("examples/web-url.yml");
    }

    private void deserializeServiceSpec(String fileName) throws Exception {
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        DefaultServiceSpec serviceSpec = YAMLServiceSpecFactory
                .generateServiceSpec(generateRawSpecFromYAML(file));
        Assert.assertNotNull(serviceSpec);
        Assert.assertEquals(8080, serviceSpec.getApiPort());
        DefaultServiceSpec.getFactory(serviceSpec, Collections.emptyList());
    }

    private void validateServiceSpec(String fileName) throws Exception {
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        DefaultServiceSpec serviceSpec = YAMLServiceSpecFactory.generateServiceSpec(generateRawSpecFromYAML(file));

        TestingServer testingServer = new TestingServer();
        StateStoreCache.resetInstanceForTests();

        Capabilities capabilities = mock(Capabilities.class);
        when(capabilities.supportsNamedVips()).thenReturn(true);
        when(capabilities.supportsRLimits()).thenReturn(true);

        DefaultScheduler.newBuilder(serviceSpec)
                .setStateStore(DefaultScheduler.createStateStore(serviceSpec, testingServer.getConnectString()))
                .setConfigStore(DefaultScheduler.createConfigStore(serviceSpec, testingServer.getConnectString()))
                .setCapabilities(capabilities)
                .build();
        testingServer.close();
    }
}