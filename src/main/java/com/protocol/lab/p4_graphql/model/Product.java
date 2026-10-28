package com.protocol.lab.p4_graphql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Category category;
    
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    public Double getRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Review r : reviews) {
            sum += r.getScore();
        }
        return Math.round((sum / reviews.size()) * 10.0) / 10.0;
    }
}
