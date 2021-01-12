package engineering.everest.lhotse.security.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Api(consumes = APPLICATION_JSON_VALUE, tags = "Tokens", description = "Authentication and Authorization services")
@Log4j2
public class TokenController {

    private final TokenStore tokenStore;

    @Autowired
    public TokenController(@Qualifier("authTokenStore") TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @PostMapping("/tokens/refresh-token/revocation")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Revoke given refresh tokens")
    @ResponseBody
    public void revokeRefreshToken(@RequestParam("token") List<String> tokens) {
        tokens.forEach(tokenValue -> {
            OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(tokenValue);
            if (refreshToken != null) {
                LOGGER.info("Revoke refresh token: {}", refreshToken);
                tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);
                tokenStore.removeRefreshToken(refreshToken);
            }
        });
    }

    @PostMapping("/tokens/access-token/revocation")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Revoke given access tokens")
    @ResponseBody
    public void revokeAccessToken(@RequestParam("token") List<String> tokens) {
        tokens.forEach(tokenValue -> {
            OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
            if (accessToken != null) {
                LOGGER.info("Revoke access token: {}", accessToken);
                tokenStore.removeAccessToken(accessToken);
                if (accessToken.getRefreshToken() != null) {
                    tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                }
            }
        });
    }
}
