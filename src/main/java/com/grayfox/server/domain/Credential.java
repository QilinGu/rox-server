package com.grayfox.server.domain;

import java.io.Serializable;

public class Credential implements Serializable {

    private static final long serialVersionUID = 6767036404149116196L;

    private String accessToken;
    private String foursquareAccessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getFoursquareAccessToken() {
        return foursquareAccessToken;
    }

    public void setFoursquareAccessToken(String foursquareAccessToken) {
        this.foursquareAccessToken = foursquareAccessToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((foursquareAccessToken == null) ? 0 : foursquareAccessToken.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Credential other = (Credential) obj;
        if (accessToken == null) {
            if (other.accessToken != null) return false;
        } else if (!accessToken.equals(other.accessToken)) return false;
        if (foursquareAccessToken == null) {
            if (other.foursquareAccessToken != null) return false;
        } else if (!foursquareAccessToken.equals(other.foursquareAccessToken)) return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Credential [accessToken=").append(accessToken).append(", foursquareAccessToken=").append(foursquareAccessToken).append("]");
        return builder.toString();
    }
}