package engineering.everest.starterkit.axon;

import com.hazelcast.core.HazelcastInstance;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KubernetesAwareHazelcastHealthIndicator implements HealthIndicator {

    private final HazelcastInstance hazelcastInstance;
    private final boolean isRunningInKubernetes;

    @Autowired
    public KubernetesAwareHazelcastHealthIndicator(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.isRunningInKubernetes = determineIfRunningInKubernetes();
    }

    @Override
    public Health health() {
        var partitionService = hazelcastInstance.getPartitionService();
        var cluster = hazelcastInstance.getCluster();

        return Health.up()
                .withDetail("kubernetes", isRunningInKubernetes)
                .withDetail("instance-name", hazelcastInstance.getName())
                .withDetail("cluster-size", cluster.getMembers().size())
                .withDetail("cluster-state", cluster.getClusterState())
                .withDetail("cluster-time", cluster.getClusterTime())
                .withDetail("cluster-version", cluster.getClusterVersion())
                .withDetail("cluster-safe", partitionService.isClusterSafe())
                .build();
    }

    private boolean determineIfRunningInKubernetes() { // TODO: replace with @ConditionalOnCloudPlatform()
        try {
            ClientBuilder.cluster().build();
        } catch (IOException e) {
            return false; // Throws if service account token not present
        }
        return true;
    }
}
