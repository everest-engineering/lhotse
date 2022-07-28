package engineering.everest.lhotse.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
public class User implements Identifiable {

    private UUID id;
    private String displayName;
    private String emailAddress;
}
