package engineering.everest.starterkit.axon.common.services;

import engineering.everest.starterkit.axon.common.domain.Identifiable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DefaultReadServiceProviderTest {

    @Mock
    private ReadService<? extends Identifiable> readService;

    private DefaultReadServiceProvider defaultReadServiceProvider;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        defaultReadServiceProvider = new DefaultReadServiceProvider(List.of(readService));
    }

    @Test
    void shouldGetReadServiceByClass() {
        assertNotNull(defaultReadServiceProvider.getService(Identifiable.class));
    }

    @Test
    void shouldGetReadServiceByClassName() {
        assertNotNull(defaultReadServiceProvider.getService(Identifiable.class.getSimpleName()));
    }
}
