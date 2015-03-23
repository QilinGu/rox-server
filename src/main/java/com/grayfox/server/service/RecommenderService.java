package com.grayfox.server.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.grayfox.server.dao.CredentialDao;
import com.grayfox.server.dao.RecommendationDao;
import com.grayfox.server.datasource.PoiDataSource;
import com.grayfox.server.domain.Category;
import com.grayfox.server.domain.Location;
import com.grayfox.server.domain.Poi;
import com.grayfox.server.domain.Recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommenderService {

    private static final int DEFAULT_RADIUS = 20_000;
    private static final int MAX_POIS_PER_ROUTE = 6;
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommenderService.class);

    @Inject private RecommendationDao recommendationDao;
    @Inject private PoiDataSource poiDataSource;
    @Inject private CredentialDao credentialDao;

    @Transactional(readOnly = true)
    public List<Recommendation> recommendByAll(String accessToken, Location location, Integer radius, Locale locale) {
        if (!credentialDao.existsAccessToken(accessToken)) {
            LOGGER.warn("Not existing user attempting to retrive information");
            throw new ServiceException.Builder("user.invalid.error").build();
        }
        List<Recommendation> recommendations = recommendationDao.fetchNearestByCategoriesLiked(accessToken, location, radius != null ? radius.intValue() : DEFAULT_RADIUS, locale);
        List<Set<Category>> categorieSets = recommendations.stream().map(Recommendation::getPoi).collect(Collectors.toList()).stream().map(Poi::getCategories).collect(Collectors.toList());
        Set<Category> categories = new HashSet<>();
        categorieSets.forEach(categorySet -> categories.addAll(categorySet));
        List<Recommendation> recommendationsByFriendsLikes = recommendationDao.fetchNearestByCategoriesLikedByFriends(accessToken, location, radius != null ? radius.intValue() : DEFAULT_RADIUS, locale);
        recommendationsByFriendsLikes = recommendationsByFriendsLikes.stream().filter(recommendation -> Collections.disjoint(categories, recommendation.getPoi().getCategories())).collect(Collectors.toList());
        recommendations.addAll(recommendationsByFriendsLikes);
        return recommendations;
    }

    @Transactional(readOnly = true)
    public List<Recommendation> recommendByLikes(String accessToken, Location location, Integer radius, Locale locale) {
        if (!credentialDao.existsAccessToken(accessToken)) {
            LOGGER.warn("Not existing user attempting to retrive information");
            throw new ServiceException.Builder("user.invalid.error").build();
        }
        return recommendationDao.fetchNearestByCategoriesLiked(accessToken, location, radius != null ? radius.intValue() : DEFAULT_RADIUS, locale);        
    }

    @Transactional(readOnly = true)
    public List<Recommendation> recommendByFriendsLikes(String accessToken, Location location, Integer radius, Locale locale) {
        if (!credentialDao.existsAccessToken(accessToken)) {
            LOGGER.warn("Not existing user attempting to retrive information");
            throw new ServiceException.Builder("user.invalid.error").build();
        }
        return recommendationDao.fetchNearestByCategoriesLikedByFriends(accessToken, location, radius != null ? radius.intValue() : DEFAULT_RADIUS, locale);        
    }

    @Transactional(readOnly = true)
    public List<Poi> nextPois(String accessToken, Poi seed, Locale locale) {
        if (!credentialDao.existsAccessToken(accessToken)) {
            LOGGER.warn("Not existing user attempting to retrive information");
            throw new ServiceException.Builder("user.invalid.error").build();
        }
        return poiDataSource.nextPois(seed, MAX_POIS_PER_ROUTE, locale);
    }
}