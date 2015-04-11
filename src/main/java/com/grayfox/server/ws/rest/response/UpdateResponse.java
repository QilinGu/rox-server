package com.grayfox.server.ws.rest.response;

import java.io.Serializable;

public class UpdateResponse implements Serializable {

    private static final long serialVersionUID = 1450022070052339450L;

    private final boolean success;

    public UpdateResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (success ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UpdateResponse other = (UpdateResponse) obj;
        if (success != other.success) return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UpdateResponse [success=").append(success).append("]");
        return builder.toString();
    }
}