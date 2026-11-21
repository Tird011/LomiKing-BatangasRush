package com.lomiking.core.customer;

/** 1-5 stars plus a short comment. */
public final class Rating {
    private final int stars;
    private final String comment;
    public Rating(int stars, String comment) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("stars 1..5");
        this.stars = stars; this.comment = comment;
    }
    public int getStars() { return stars; }
    public String getComment() { return comment; }
}
