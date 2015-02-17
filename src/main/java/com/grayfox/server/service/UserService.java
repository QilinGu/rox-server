package com.grayfox.server.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import com.grayfox.server.dao.CredentialDao;
import com.grayfox.server.dao.UserDao;
import com.grayfox.server.datasource.ProfileDataSource;
import com.grayfox.server.domain.Credential;
import com.grayfox.server.domain.User;
import com.grayfox.server.oauth.SocialNetworkAuthenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Inject private UserDao userDao;
    @Inject private CredentialDao credentialDao;
    @Inject @Named("foursquareAuthenticator") private SocialNetworkAuthenticator foursquareAuthenticator;
    @Inject @Named("profileFoursquareDataSource") private ProfileDataSource profileFoursquareDataSource;

    @Transactional
    public Credential registerUsingFoursquare(String authorizationCode) {
        String accessToken = foursquareAuthenticator.exchangeAccessToken(authorizationCode);
        Credential credential = credentialDao.fetchByFoursquareAccessToken(accessToken);
        if (credential != null) {
            LOGGER.debug("Credential already exists");
            return credential;
        } else {
            credential = new Credential();
            credential.setFoursquareAccessToken(accessToken);
            credential.setAccessToken(generateAccessToken());
            credential.setNew(true);
            credentialDao.save(credential);
            LOGGER.debug("New credential created");
            return credential;
        }
    }

    @Async
    @Transactional
    public void generateProfileUsingFoursquare(Credential credential) {
        profileFoursquareDataSource.setAccessToken(credential.getFoursquareAccessToken());
        User user = profileFoursquareDataSource.collectUserData();
        user.setLikes(profileFoursquareDataSource.collectLikes());
        user.setFriends(profileFoursquareDataSource.collectFriendsAndLikes());
        user.setCredential(credential);
        userDao.saveOrUpdate(user);
    }

    @Transactional(readOnly = true)
    public User getCompactSelf(String accessToken) {
        if (!credentialDao.existsAccessToken(accessToken)) {
            LOGGER.warn("Not existing user attempting to retrive information");
            throw new ServiceException.Builder("user.invalid.error").build();
        }
        return userDao.fetchCompactByAccessToken(accessToken);
    }

    private String generateAccessToken() {
        String accessToken = null;
        do accessToken = UUID.randomUUID().toString().replaceAll("-", ""); while (credentialDao.existsAccessToken(accessToken));
        return accessToken;
    }
}