package com.protocol.lab.p4_graphql;

import com.protocol.lab.p4_graphql.controller.ProductGraphQLController;
import com.protocol.lab.p4_graphql.model.Category;
import com.protocol.lab.p4_graphql.model.Product;
import com.protocol.lab.p4_graphql.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.assertj.core.api.Assertions.assertThat;

@GraphQlTest(ProductGraphQLController.class)
@Import(ProductRepository.class)
class ProductGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    @DisplayName("Should query all products with nested reviews and computed rating")
    void shouldQueryProducts() {
        String document = """
            query {
                products {
                    id
                    name
                    price
                    category
                    rating
                    reviews {
                        author
                        score
                    }
                }
            }
        """;

        graphQlTester.document(document)
                .execute()
                .path("products")
                .entityList(Product.class)
                .satisfies(products -> {
                    assertThat(products).isNotEmpty();
                    assertThat(products.get(0).getName()).isNotBlank();
                });
    }

    @Test
    @DisplayName("Should query single product by ID")
    void shouldQueryProductById() {
        String document = """
            query {
                productById(id: "PROD-101") {
                    id
                    name
                    category
                }
            }
        """;

        graphQlTester.document(document)
                .execute()
                .path("productById.name")
                .entity(String.class)
                .isEqualTo("MacBook Pro 16 M3 Max");
    }

    @Test
    @DisplayName("Should create product via GraphQL mutation")
    void shouldCreateProduct() {
        String document = """
            mutation {
                createProduct(input: {
                    name: "Kubernetes Up and Running",
                    description: "Dive into cloud-native",
                    price: 39.99,
                    category: BOOKS
                }) {
                    id
                    name
                    price
                    category
                }
            }
        """;

        graphQlTester.document(document)
                .execute()
                .path("createProduct.name")
                .entity(String.class)
                .isEqualTo("Kubernetes Up and Running")
                .path("createProduct.category")
                .entity(Category.class)
                .isEqualTo(Category.BOOKS);
    }
}
