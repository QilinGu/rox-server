/*
 * Copyright 2014-2015 Daniel Pedraza-Arcega
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.grayfox.server.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import com.grayfox.server.dao.CategoryDao;
import com.grayfox.server.dao.CredentialDao;
import com.grayfox.server.dao.PoiDao;
import com.grayfox.server.dao.RecommendationDao;
import com.grayfox.server.domain.Category;
import com.grayfox.server.domain.Location;
import com.grayfox.server.domain.Poi;
import com.grayfox.server.domain.Recommendation;
import com.grayfox.server.util.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PoiService {

    private static final int MAX_POIS_PER_ROUTE = 6;
    private static final Logger LOGGER = LoggerFactory.getLogger(PoiService.class);

    @Inject private CredentialDao credentialDao;
    @Inject private CategoryDao categoryDao;
    @Inject private RecommendationDao recommendationDao;
    @Inject private PoiDao poiDao;

    @Transactional(readOnly = true)
    public List<Poi> getNearestPoisByCategory(Location location, int radius, String categoryFoursquareId, Locale locale) {
        return poiDao.fetchNearestByCategory(location, radius, categoryFoursquareId, locale);
    }

    @Transactional(readOnly = true)
    public List<Poi> buildRoute(String poiFoursquareId, Locale locale) {
        return poiDao.fetchNext(poiFoursquareId, MAX_POIS_PER_ROUTE, locale);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesLikeName(String partialName, Locale locale) {
        return categoryDao.fetchLikeName(partialName, locale);
    }

    @Transactional(readOnly = true)
    public List<Recommendation> recommend(String accessToken, Location location, int radius, Locale locale) {
        Set<String> categories = new HashSet<>();
        List<Recommendation> recommendations = new ArrayList<>();
        if (accessToken != null) {
            LOGGER.debug("Adding personalized recommendations...");
            if (!credentialDao.existsAccessToken(accessToken)) {
                LOGGER.warn("Not existing user attempting to retrive information");
                throw new ServiceException.Builder()
                    .messageKey("user.invalid.error")
                    .build();
            }
            List<Recommendation> recommendationsByCategoriesLiked = recommendationDao.fetchNearestByCategoriesLiked(accessToken, location, radius, locale);
            List<Recommendation> recommendationsByCategoriesLikedByFriends = recommendationDao.fetchNearestByCategoriesLikedByFriends(accessToken, location, radius, locale);
            recommendations.addAll(recommendationsByCategoriesLiked);
            recommendations.addAll(recommendationsByCategoriesLikedByFriends);
        } else LOGGER.debug("Only global recommendations...");
        List<Recommendation> recommendationsByRating = recommendationDao.fetchNearestByRating(location, radius, locale);
        recommendations.addAll(recommendationsByRating);
        for (Iterator<Recommendation> iterator = recommendations.iterator(); iterator.hasNext();) {
            Recommendation recommendation = iterator.next();
            for (Category category : recommendation.getPoi().getCategories()) {
                if (!categories.add(category.getFoursquareId())) {
                    iterator.remove();
                    break;
                }
            }
        }
        recommendations.sort((r1, r2) -> {
            double distance1 = distanceBetween(location, r1.getPoi().getLocation());
            double distance2 = distanceBetween(location, r2.getPoi().getLocation());
            return distance1 > distance2 ? 1 : distance1 < distance2 ? -1 : 0;
        });
        return recommendations;
    }

    private double distanceBetween(Location location1, Location location2) {
        double fi1 = Math.toRadians(location1.getLatitude());
        double fi2 = Math.toRadians(location2.getLatitude());
        double deltaLambda = Math.toRadians(location2.getLongitude()-location1.getLongitude());
        return Math.acos(Math.sin(fi1)*Math.sin(fi2) + Math.cos(fi1)*Math.cos(fi2)*Math.cos(deltaLambda)) * Constants.Ints.EARTH_RADIUS; 
    }
}