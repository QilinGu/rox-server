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
package com.grayfox.server.dao.jdbc;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.grayfox.server.dao.RecommendationDao;
import com.grayfox.server.domain.Category;
import com.grayfox.server.domain.Location;
import com.grayfox.server.domain.Poi;
import com.grayfox.server.domain.Recommendation;
import com.grayfox.server.util.Messages;

import org.springframework.stereotype.Repository;

@Repository("recommendationLocalDao")
public class RecommendationJdbcDao extends JdbcDao implements RecommendationDao {

    @Override
    public List<Recommendation> fetchNearestByRating(Location location, Integer radius, Locale locale) {
        List<Recommendation> recommendations = getJdbcTemplate().query(getQuery("nearestRecommendationsByRating"), 
                (ResultSet rs, int i) -> {
                        Recommendation recommendation = new Recommendation();
                        Poi poi = new Poi();
                        poi.setName(rs.getString(1));
                        poi.setLocation(new Location());
                        poi.getLocation().setLatitude(rs.getDouble(2));
                        poi.getLocation().setLongitude(rs.getDouble(3));
                        poi.setFoursquareId(rs.getString(4));
                        poi.setFoursquareRating(rs.getDouble(5));
                        recommendation.setType(Recommendation.Type.GLOBAL);
                        recommendation.setReason(Messages.get("recommendation.global.reason", locale));
                        recommendation.setPoi(poi);
                        return recommendation;
                }, location.getLatitude(), location.getLongitude(), radius);
        recommendations.forEach(recommendation -> recommendation.getPoi().setCategories(new HashSet<>(fetchCategoriesByPoiFoursquareId(recommendation.getPoi().getFoursquareId(), locale))));
        return recommendations;
    }

    @Override
    public List<Recommendation> fetchNearestByCategoriesLiked(String accessToken, Location location, Integer radius, Locale locale) {
        Set<String> categoryNames = new HashSet<>();
        List<Recommendation> recommendations = getJdbcTemplate().query(getQuery("nearestRecommendationsByCategoriesLiked", locale), 
                (ResultSet rs, int i) -> {
                    String categoryName = rs.getString(6);
                    if (categoryNames.add(categoryName)) {
                        Recommendation recommendation = new Recommendation();
                        Poi poi = new Poi();
                        poi.setName(rs.getString(1));
                        poi.setLocation(new Location());
                        poi.getLocation().setLatitude(rs.getDouble(2));
                        poi.getLocation().setLongitude(rs.getDouble(3));
                        poi.setFoursquareId(rs.getString(4));
                        poi.setFoursquareRating(rs.getDouble(5));
                        recommendation.setType(Recommendation.Type.SELF);
                        recommendation.setReason(Messages.get("recommendation.self.reason", locale, categoryName));
                        recommendation.setPoi(poi);
                        return recommendation;
                    } else return null;
                }, accessToken, location.getLatitude(), location.getLongitude(), radius);
        recommendations = recommendations.stream().filter(Objects::nonNull).collect(Collectors.toList());
        recommendations.forEach(recommendation -> recommendation.getPoi().setCategories(new HashSet<>(fetchCategoriesByPoiFoursquareId(recommendation.getPoi().getFoursquareId(), locale))));
        return recommendations;
    }

    @Override
    public List<Recommendation> fetchNearestByCategoriesLikedByFriends(String accessToken, Location location, Integer radius, Locale locale) {
        Set<String> categoryNames = new HashSet<>();
        List<Recommendation> recommendations = getJdbcTemplate().query(getQuery("nearestRecommendationsByCategoriesLikedByFriends", locale), 
                (ResultSet rs, int i) -> {
                    String categoryName = rs.getString(8);
                    if (categoryNames.add(categoryName)) {
                        Recommendation recommendation = new Recommendation();
                        Poi poi = new Poi();
                        poi.setName(rs.getString(1));
                        poi.setLocation(new Location());
                        poi.getLocation().setLatitude(rs.getDouble(2));
                        poi.getLocation().setLongitude(rs.getDouble(3));
                        poi.setFoursquareId(rs.getString(4));
                        poi.setFoursquareRating(rs.getDouble(5));
                        recommendation.setType(Recommendation.Type.SOCIAL);
                        String lastName = rs.getString(7);
                        String friendName = lastName == null || lastName.trim().isEmpty() ? rs.getString(6) : rs.getString(6) + ' ' + lastName;
                        recommendation.setReason(Messages.get("recommendation.social.reason", locale, friendName, categoryName));
                        recommendation.setPoi(poi);
                        return recommendation;
                    } else return null;
                }, accessToken, location.getLatitude(), location.getLongitude(), radius);
        recommendations = recommendations.stream().filter(Objects::nonNull).collect(Collectors.toList());
        recommendations.forEach(recommendation -> recommendation.getPoi().setCategories(new HashSet<>(fetchCategoriesByPoiFoursquareId(recommendation.getPoi().getFoursquareId(), locale))));
        return recommendations;
    }

    private List<Category> fetchCategoriesByPoiFoursquareId(String foursquareId, Locale locale) {
        return getJdbcTemplate().query(getQuery("categoriesByPoiFoursquareId", locale), 
                (ResultSet rs, int i) -> {
                    Category category = new Category();
                    category.setName(rs.getString(1));
                    category.setIconUrl(rs.getString(2));
                    category.setFoursquareId(rs.getString(3));
                    return category;
                }, foursquareId);
    }
}