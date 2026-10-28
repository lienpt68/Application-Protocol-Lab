package com.protocol.lab.p4_graphql.repository;

import com.protocol.lab.p4_graphql.model.Category;
import com.protocol.lab.p4_graphql.model.Product;
import com.protocol.lab.p4_graphql.model.Review;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private final Map<String, Product> productMap = new ConcurrentHashMap<>();
    private final Map<String, List<Review>> reviewMap = new ConcurrentHashMap<>();
    private final AtomicLong productIdGen = new AtomicLong(100);
    private final AtomicLong reviewIdGen = new AtomicLong(500);

    @PostConstruct
    public void initData() {
        Product p1 = Product.builder()
                .id("PROD-101")
                .name("MacBook Pro 16 M3 Max")
                .description("Apple Silicon, 36GB RAM, 1TB SSD")
                .price(3499.0)
                .category(Category.ELECTRONICS)
                .reviews(new ArrayList<>())
                .build();

        Product p2 = Product.builder()
                .id("PROD-102")
                .name("Designing Data-Intensive Applications")
                .description("Martin Kleppmann - The distributed systems bible")
                .price(45.0)
                .category(Category.BOOKS)
                .reviews(new ArrayList<>())
                .build();

        Product p3 = Product.builder()
                .id("PROD-103")
                .name("Ergonomic Mechanical Keyboard")
                .description("Split design, Gateron Brown switches")
                .price(189.0)
                .category(Category.ELECTRONICS)
                .reviews(new ArrayList<>())
                .build();

        productMap.put(p1.getId(), p1);
        productMap.put(p2.getId(), p2);
        productMap.put(p3.getId(), p3);

        addReview(p1.getId(), "Alice", "Top-tier performance for Docker & JVM!", 5);
        addReview(p1.getId(), "Bob", "Expensive but totally worth it.", 4);
        addReview(p2.getId(), "Architect-Q", "Must read for every Software & Solution Architect.", 5);
    }

    public List<Product> findAll(Category category, Double minPrice, Double maxPrice) {
        return productMap.values().stream()
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .map(this::attachReviews)
                .collect(Collectors.toList());
    }

    public Optional<Product> findById(String id) {
        Product p = productMap.get(id);
        if (p == null) return Optional.empty();
        return Optional.of(attachReviews(p));
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId("PROD-" + productIdGen.incrementAndGet());
        }
        if (product.getReviews() == null) {
            product.setReviews(new ArrayList<>());
        }
        productMap.put(product.getId(), product);
        return attachReviews(product);
    }

    public boolean deleteById(String id) {
        reviewMap.remove(id);
        return productMap.remove(id) != null;
    }

    public Review addReview(String productId, String author, String comment, int score) {
        Review review = Review.builder()
                .id("REV-" + reviewIdGen.incrementAndGet())
                .productId(productId)
                .author(author)
                .comment(comment)
                .score(score)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        reviewMap.computeIfAbsent(productId, k -> new ArrayList<>()).add(review);
        return review;
    }

    public List<Review> findReviewsByProductId(String productId) {
        return new ArrayList<>(reviewMap.getOrDefault(productId, Collections.emptyList()));
    }

    public Map<String, List<Review>> findReviewsForProducts(List<String> productIds) {
        Map<String, List<Review>> result = new HashMap<>();
        for (String id : productIds) {
            result.put(id, findReviewsByProductId(id));
        }
        return result;
    }

    public List<Category> findAllCategories() {
        return Arrays.asList(Category.values());
    }

    private Product attachReviews(Product p) {
        List<Review> revs = reviewMap.getOrDefault(p.getId(), Collections.emptyList());
        p.setReviews(new ArrayList<>(revs));
        return p;
    }
}
