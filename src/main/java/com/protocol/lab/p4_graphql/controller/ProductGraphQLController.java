package com.protocol.lab.p4_graphql.controller;

import com.protocol.lab.p4_graphql.model.*;
import com.protocol.lab.p4_graphql.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProductGraphQLController {

    private final ProductRepository productRepository;

    @QueryMapping
    public List<Product> products(@Argument Category category,
                                  @Argument Double minPrice,
                                  @Argument Double maxPrice) {
        log.info("GraphQL Query: products (category={}, minPrice={}, maxPrice={})", category, minPrice, maxPrice);
        return productRepository.findAll(category, minPrice, maxPrice);
    }

    @QueryMapping
    public Optional<Product> productById(@Argument String id) {
        log.info("GraphQL Query: productById (id={})", id);
        return productRepository.findById(id);
    }

    @QueryMapping
    public List<Category> categories() {
        log.info("GraphQL Query: categories");
        return productRepository.findAllCategories();
    }

    @MutationMapping
    public Product createProduct(@Argument ProductInput input) {
        log.info("GraphQL Mutation: createProduct (name={}, price={}, category={})", 
                input.getName(), input.getPrice(), input.getCategory());
        Product p = Product.builder()
                .name(input.getName())
                .description(input.getDescription())
                .price(input.getPrice())
                .category(input.getCategory())
                .build();
        return productRepository.save(p);
    }

    @MutationMapping
    public Review addReview(@Argument ReviewInput input) {
        log.info("GraphQL Mutation: addReview (productId={}, author={}, score={})",
                input.getProductId(), input.getAuthor(), input.getScore());
        return productRepository.addReview(
                input.getProductId(),
                input.getAuthor(),
                input.getComment(),
                input.getScore()
        );
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument String id) {
        log.info("GraphQL Mutation: deleteProduct (id={})", id);
        return productRepository.deleteById(id);
    }
}
