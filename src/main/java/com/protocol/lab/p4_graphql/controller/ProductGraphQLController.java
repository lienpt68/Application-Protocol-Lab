package com.protocol.lab.p4_graphql.controller;

import com.protocol.lab.p4_graphql.model.*;
import com.protocol.lab.p4_graphql.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.*;

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

    /**
     * The Data Engineer Way: Giải quyết triệt để bài toán N+1 Query trong GraphQL.
     * Thay vì gọi N queries tới DB cho từng Product để lấy reviews,
     * @BatchMapping gom toàn bộ danh sách products vào 1 lần gọi duy nhất qua DataLoader pattern.
     */
    @BatchMapping(typeName = "Product", field = "reviews")
    public Map<Product, List<Review>> reviews(List<Product> products) {
        log.info("[DataLoader BatchMapping] Batch loading reviews for {} products in 1 batch call", products.size());
        List<String> productIds = products.stream().map(Product::getId).toList();
        Map<String, List<Review>> reviewsByProductId = productRepository.findReviewsForProducts(productIds);

        Map<Product, List<Review>> result = new HashMap<>();
        for (Product p : products) {
            result.put(p, reviewsByProductId.getOrDefault(p.getId(), Collections.emptyList()));
        }
        return result;
    }

    @SchemaMapping(typeName = "Product", field = "rating")
    public Double rating(Product product) {
        List<Review> revs = productRepository.findReviewsByProductId(product.getId());
        if (revs.isEmpty()) return 0.0;
        double sum = revs.stream().mapToInt(Review::getScore).sum();
        return Math.round((sum / revs.size()) * 10.0) / 10.0;
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
