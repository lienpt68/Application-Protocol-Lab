# GraphQL API Documentation & Testing Guide

## 1. Test qua Trình duyệt (GraphiQL IDE)
- Truy cập trực tiếp: `http://localhost:8080/graphiql`
- GraphiQL hỗ trợ autocomplete (Ctrl+Space), xem docs schema và chạy test trực quan.

---

## 2. Test trực tiếp trên Postman (Native GraphQL)

### Bước 1: Tạo Request
- Trên Postman, bấm **New** -> Chọn **HTTP Request**.
- Đổi Method thành **POST**.
- **URL**: `http://localhost:8080/graphql`
- Chọn tab **Body** -> Chọn radio button **GraphQL**.

---

### Bước 2: Truy vấn danh sách sản phẩm & Reviews (Test BatchMapping)
Dán câu Query sau vào ô **Query**:
```graphql
query GetAllProducts {
  products {
    id
    name
    price
    category
    rating
    reviews {
      id
      author
      comment
      score
    }
  }
}
```
Bấm **Send** để nhận kết quả JSON. Lưu ý quan sát log server để thấy `@BatchMapping` chỉ gọi đúng 1 lần cho toàn bộ sản phẩm.

---

### Bước 3: Lọc sản phẩm theo Category & Giá (Variables)
Dán Query:
```graphql
query GetFilteredProducts($category: Category, $minPrice: Float, $maxPrice: Float) {
  products(category: $category, minPrice: $minPrice, maxPrice: $maxPrice) {
    id
    name
    price
    category
    rating
  }
}
```
Dán JSON sau vào ô **GraphQL Variables**:
```json
{
  "category": "ELECTRONICS",
  "minPrice": 100.0,
  "maxPrice": 4000.0
}
```

---

### Bước 4: Tạo sản phẩm mới (Mutation)
Dán Query:
```graphql
mutation CreateNewProduct($input: ProductInput!) {
  createProduct(input: $input) {
    id
    name
    description
    price
    category
  }
}
```
Dán GraphQL Variables:
```json
{
  "input": {
    "name": "Sony WH-1000XM5 Wireless Headphones",
    "description": "Industry Leading Noise Canceling",
    "price": 398.0,
    "category": "ELECTRONICS"
  }
}
```

---

### Bước 5: Thêm đánh giá (Mutation Add Review)
Dán Query:
```graphql
mutation AddReviewToProduct($input: ReviewInput!) {
  addReview(input: $input) {
    id
    productId
    author
    comment
    score
    createdAt
  }
}
```
Dán GraphQL Variables:
```json
{
  "input": {
    "productId": "PROD-101",
    "author": "TechReviewer",
    "comment": "Quá mạnh, render 4K video và compile Java siêu tốc!",
    "score": 5
  }
}
```

---

## 3. Test nhanh qua cURL

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { products { id name price category rating } }"}'
```
