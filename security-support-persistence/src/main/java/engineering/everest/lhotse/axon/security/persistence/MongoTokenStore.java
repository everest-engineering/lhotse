package engineering.everest.lhotse.axon.security.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

@Component
public class MongoTokenStore implements TokenStore {

    private final OAuthAccessTokenRepository oauthAccessTokenRepository;
    private final OAuthRefreshTokenRepository oauthRefreshTokenRepository;
    private final AuthenticationKeyGenerator authenticationKeyGenerator;
    private final TokenKeyGenerator tokenKeyGenerator;
    private final OAuth2Serializer oauth2Serializer;

    @Autowired
    public MongoTokenStore(OAuthAccessTokenRepository oauthAccessTokenRepository,
                           OAuthRefreshTokenRepository oauthRefreshTokenRepository,
                           AuthenticationKeyGenerator authenticationKeyGenerator,
                           TokenKeyGenerator tokenKeyGenerator,
                           OAuth2Serializer oauth2Serializer) {
        this.oauthAccessTokenRepository = oauthAccessTokenRepository;
        this.oauthRefreshTokenRepository = oauthRefreshTokenRepository;
        this.authenticationKeyGenerator = authenticationKeyGenerator;
        this.tokenKeyGenerator = tokenKeyGenerator;
        this.oauth2Serializer = oauth2Serializer;
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String tokenValue) {
        String tokenId = tokenKeyGenerator.extractKey(tokenValue);

        PersistableOAuthAccessToken persistableOAuthAccessToken = oauthAccessTokenRepository.findByTokenId(tokenId);

        if (persistableOAuthAccessToken != null) {
            try {
                return oauth2Serializer.deserializeAuthentication(persistableOAuthAccessToken.getAuthentication());
            } catch (IllegalArgumentException e) {
                removeAccessTokenByValue(tokenValue);
            }
        }

        return null;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token,
                                 OAuth2Authentication authentication) {
        String refreshToken = null;
        if (token.getRefreshToken() != null) {
            refreshToken = token.getRefreshToken().getValue();
        }

        if (readAccessToken(token.getValue()) != null) {
            removeAccessTokenByValue(token.getValue());
        }

        String tokenKey = tokenKeyGenerator.extractKey(token.getValue());

        PersistableOAuthAccessToken persistableOAuthAccessToken = new PersistableOAuthAccessToken(tokenKey,
                oauth2Serializer.serializeAccessToken(token),
                authenticationKeyGenerator.extractKey(authentication),
                authentication.isClientOnly() ? null : authentication.getName(),
                authentication.getOAuth2Request().getClientId(),
                oauth2Serializer.serializeAuthentication(authentication),
                tokenKeyGenerator.extractKey(refreshToken));

        oauthAccessTokenRepository.save(persistableOAuthAccessToken);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        String tokenKey = tokenKeyGenerator.extractKey(tokenValue);
        PersistableOAuthAccessToken persistableOAuthAccessToken = oauthAccessTokenRepository.findByTokenId(tokenKey);
        if (persistableOAuthAccessToken != null) {
            try {
                return oauth2Serializer.deserializeAccessToken(persistableOAuthAccessToken.getToken());
            } catch (IllegalArgumentException e) {
                removeAccessTokenByValue(tokenValue);
            }
        }
        return null;
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        removeAccessTokenByValue(token.getValue());
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken,
                                  OAuth2Authentication authentication) {
        String tokenKey = tokenKeyGenerator.extractKey(refreshToken.getValue());
        byte[] token = oauth2Serializer.serializeRefreshToken(refreshToken);
        byte[] authenticationBytes = oauth2Serializer.serializeAuthentication(authentication);

        PersistableOAuthRefreshToken persistableOAuthRefreshToken = new PersistableOAuthRefreshToken(tokenKey, token, authenticationBytes);

        oauthRefreshTokenRepository.save(persistableOAuthRefreshToken);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        String tokenKey = tokenKeyGenerator.extractKey(tokenValue);
        PersistableOAuthRefreshToken persistableOAuthRefreshToken = oauthRefreshTokenRepository.findByTokenId(tokenKey);

        if (persistableOAuthRefreshToken != null) {
            try {
                return oauth2Serializer.deserializeRefreshToken(persistableOAuthRefreshToken.getToken());
            } catch (IllegalArgumentException e) {
                removeRefreshTokenByValue(tokenValue);
            }
        }

        return null;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthenticationForRefreshTokenValue(token.getValue());
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        removeRefreshTokenByValue(token.getValue());
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        removeAccessTokenUsingRefreshTokenValue(refreshToken.getValue());
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        String key = authenticationKeyGenerator.extractKey(authentication);
        List<PersistableOAuthAccessToken> persistableOAuthAccessTokens = oauthAccessTokenRepository.findByAuthenticationId(key);
        if (persistableOAuthAccessTokens.isEmpty()) {
            return null;
        }

        OAuth2AccessToken accessToken = oauth2Serializer.deserializeAccessToken(persistableOAuthAccessTokens.get(0).getToken());
        if (accessToken != null
                && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
            removeAccessTokenByValue(accessToken.getValue());
            // Keep the store consistent (maybe the same user is represented by this authentication but the details have
            // changed)
            storeAccessToken(accessToken, authentication);
        }
        return accessToken;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String username) {
        List<PersistableOAuthAccessToken> persistableOAuthAccessTokens = oauthAccessTokenRepository
                .findByUsernameAndClientId(username, clientId);
        return transformToOAuth2AccessTokens(persistableOAuthAccessTokens);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        List<PersistableOAuthAccessToken> persistableOAuthAccessTokens = oauthAccessTokenRepository.findByClientId(clientId);
        return transformToOAuth2AccessTokens(persistableOAuthAccessTokens);
    }

    private OAuth2Authentication readAuthenticationForRefreshTokenValue(String tokenValue) {
        String tokenId = tokenKeyGenerator.extractKey(tokenValue);

        PersistableOAuthRefreshToken persistableOAuthRefreshToken = oauthRefreshTokenRepository.findByTokenId(tokenId);

        if (persistableOAuthRefreshToken != null) {
            try {
                return oauth2Serializer.deserializeAuthentication(persistableOAuthRefreshToken.getAuthentication());
            } catch (IllegalArgumentException e) {
                removeRefreshTokenByValue(tokenValue);
            }
        }

        return null;
    }

    private void removeRefreshTokenByValue(String tokenValue) {
        String tokenId = tokenKeyGenerator.extractKey(tokenValue);
        oauthRefreshTokenRepository.deleteByTokenId(tokenId);
    }

    private void removeAccessTokenUsingRefreshTokenValue(String tokenValue) {
        String tokenId = tokenKeyGenerator.extractKey(tokenValue);
        oauthAccessTokenRepository.deleteByRefreshTokenId(tokenId);

    }

    private void removeAccessTokenByValue(String tokenValue) {
        String tokenKey = tokenKeyGenerator.extractKey(tokenValue);
        oauthAccessTokenRepository.deleteByTokenId(tokenKey);
    }

    private Collection<OAuth2AccessToken> transformToOAuth2AccessTokens(List<PersistableOAuthAccessToken> accessTokens) {
        return accessTokens.stream()
                .filter(Objects::nonNull)
                .map(token -> oauth2Serializer.deserializeAccessToken(token.getToken()))
                .collect(Collectors.toList());
    }
}
