package com.protocol.lab.p4_graphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewInput {
    private String productId;
    private String author;
    private String comment;
    private Integer score;
}
