package com.protocol.lab.p4_graphql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private String id;
    private String productId;
    private String author;
    private String comment;
    private Integer score;
    private String createdAt;
}
