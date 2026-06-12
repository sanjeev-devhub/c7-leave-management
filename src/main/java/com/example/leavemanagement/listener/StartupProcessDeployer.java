package com.example.leavemanagement.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Deploys all BPMN process models to the embedded Camunda 7 engine explicitly at
 * startup, using the {@link RepositoryService}.
 *
 * <p>Why explicit deployment instead of auto-deploy?</p>
 * <ul>
 *   <li>The Spring Boot starter's {@code deployment-resource-pattern} auto-deploy
 *       and {@code @EnableProcessApplication} + {@code META-INF/processes.xml}
 *       both depend on classpath/process-application wiring that behaves
 *       differently between a fat-jar, an IDE run, and a manual/modular engine
 *       setup. When it silently does nothing you get
 *       "no processes deployed with key 'annual-leave-request'".</li>
 *   <li>Deploying via {@code RepositoryService} is deterministic in every setup
 *       and logs exactly what was deployed.</li>
 * </ul>
 *
 * <p>{@code enableDuplicateFiltering(true)} means a model is only re-deployed
 * when its content actually changes, so restarts don't create redundant
 * deployment versions.</p>
 *
 * <p>Runs with {@link Order} highest-precedence on {@link ApplicationReadyEvent}
 * so the process is registered before anything else (e.g. the identity seeder)
 * reacts to application readiness.</p>
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class StartupProcessDeployer {

    private static final String PROCESS_LOCATION_PATTERN = "classpath*:processes/*.bpmn";

    private final RepositoryService repositoryService;

    @EventListener(ApplicationReadyEvent.class)
    public void deployProcesses() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(PROCESS_LOCATION_PATTERN);

            if (resources.length == 0) {
                log.warn("No BPMN resources found matching {} - nothing to deploy.", PROCESS_LOCATION_PATTERN);
                return;
            }

            var deploymentBuilder = repositoryService.createDeployment()
                    .name("leave-management-autodeploy")
                    .source("startup")
                    .enableDuplicateFiltering(true);

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                deploymentBuilder.addInputStream(filename, resource.getInputStream());
                log.info("Adding BPMN resource to deployment: {}", filename);
            }

            Deployment deployment = deploymentBuilder.deploy();
            log.info("Camunda deployment complete. Deployment id: {}", deployment.getId());

            // Log the resulting process definitions for visibility
            for (ProcessDefinition pd : repositoryService.createProcessDefinitionQuery()
                    .latestVersion().list()) {
                log.info("Deployed process definition: key='{}' version={} id={}",
                        pd.getKey(), pd.getVersion(), pd.getId());
            }
        } catch (Exception e) {
            log.error("Failed to deploy BPMN processes at startup", e);
            throw new IllegalStateException("BPMN deployment failed", e);
        }
    }
}