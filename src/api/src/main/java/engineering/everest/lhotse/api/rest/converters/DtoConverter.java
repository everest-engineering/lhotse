package engineering.everest.lhotse.api.rest.converters;

import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.common.domain.User;
import org.springframework.stereotype.Service;

@Service
public class DtoConverter {

    public UserResponse convert(User user) {
        return new UserResponse(user.getId(), user.getOrganizationId(), user.getDisplayName(),
            user.getEmailAddress(), user.isDisabled());
    }
}
