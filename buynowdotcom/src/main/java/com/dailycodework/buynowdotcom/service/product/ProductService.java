package com.dailycodework.buynowdotcom.service.product;

import com.dailycodework.buynowdotcom.dtos.ImageDto;
import com.dailycodework.buynowdotcom.dtos.ProductDto;
import com.dailycodework.buynowdotcom.model.*;
import com.dailycodework.buynowdotcom.repository.*;
import com.dailycodework.buynowdotcom.request.AddProductRequest;
import com.dailycodework.buynowdotcom.request.ProductUpdateRequest;
import com.dailycodework.buynowdotcom.service.embeddings.ImageSearchService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final CartItemRepository cartItemRepository;
  private final OrderItemRepository orderItemRepository;
  private final ImageRepository imageRepository;
  private final ModelMapper modelMapper;
  private final ImageSearchService imageSearchService;


  @Override
  public Product addProduct(AddProductRequest request) {
    if (productExists(request.getName(), request.getBrand())) {
      throw new EntityExistsException(request.getName() + " already exists!");
    }
    Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
            .orElseGet(() -> {
              Category newCategory = new Category(request.getCategory().getName());
              return categoryRepository.save(newCategory);
            });
    request.setCategory(category);
    return productRepository.save(createProduct(request, category));
  }

  private boolean productExists(String name, String brand) {
    return productRepository.existsByNameAndBrand(name, brand);
  }

  private Product createProduct(AddProductRequest request, Category category) {
    return new Product(
            request.getName(),
            request.getBrand(),
            request.getPrice(),
            request.getInventory(),
            request.getDescription(),
            category);
  }

  @Override
  public Product updateProduct(ProductUpdateRequest request, Long productId) {
    return productRepository.findById(productId)
            .map(existingProduct -> updateExistingProduct(existingProduct, request))
            .map(productRepository::save)
            .orElseThrow(() -> new EntityNotFoundException("Product not found!"));
  }

  private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
    existingProduct.setName(request.getName());
    existingProduct.setBrand(request.getBrand());
    existingProduct.setPrice(request.getPrice());
    existingProduct.setInventory(request.getInventory());
    existingProduct.setDescription(request.getDescription());
    Category category = categoryRepository.findByName(request.getCategory().getName());
    existingProduct.setCategory(category);
    return existingProduct;
  }

  @Override
  public void deleteProductById(Long productId) {
    productRepository.findById(productId)
            .ifPresentOrElse(product -> {
              List<CartItem> cartItems = cartItemRepository.findByProductId(productId);
              cartItems.forEach(cartItem -> {
                Cart cart = cartItem.getCart();
                cart.removeItem(cartItem);
                cartItemRepository.delete(cartItem);
              });

              List<OrderItem> orderItems = orderItemRepository.findByProductId(productId);
              orderItems.forEach(orderItem -> {
                orderItem.setProduct(null);
                orderItemRepository.save(orderItem);
              });
              Optional.ofNullable(product.getCategory())
                      .ifPresent(category -> category.getProducts().remove(product));
              product.setCategory(null);
              productRepository.deleteById(product.getId());
            }, () -> {
              throw new EntityNotFoundException("Product not found!");
            });
  }

  @Override
  public Product getProductById(Long productId) {
    return productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
  }


  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
    return productRepository.findByCategoryNameAndBrand(category, brand);
  }

  @Override
  public List<Product> getProductsByCategory(String category) {
    return productRepository.findByCategoryName(category);
  }

  @Override
  public List<Product> getProductsByBrandAndName(String brand, String name) {
    return productRepository.findByNameAndBrand(brand, name);
  }

  @Override
  public List<Product> getProductsByBrand(String brand) {
    return productRepository.findByBrand(brand);
  }

  @Override
  public List<Product> getProductsByName(String name) {
    return Optional.ofNullable(productRepository.findByName(name))
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
  }

  @Override
  public List<Product> findDistinctProductsByName() {
    List<Product> products = getAllProducts();
    Map<String, Product> distinctProductMap = products.stream()
            .collect(Collectors.toMap(
                    Product::getName,
                    product -> product,
                    (existing, replacement) -> existing));
    return new ArrayList<>(distinctProductMap.values());
  }

  @Override
  public List<String> getAllDistinctBrands() {
    return productRepository.findAll()
            .stream()
            .map(Product::getBrand)
            .distinct()
            .toList();
  }

  @Override
  public List<Product> getProductsByCategoryId(Long categoryId) {
    return productRepository.findAllByCategoryId(categoryId);
  }

  @Override
  public List<Product> searchProductsByImage(MultipartFile image) throws IOException {
    List<Long> productIds = imageSearchService.searchImageSimilarity(image);
    return productRepository.findAllById(productIds);
  }

  @Override
  public List<ProductDto> getConvertedProducts(List<Product> products) {
    return products.stream().map(this::convertToDto).toList();
  }

  @Override
  public ProductDto convertToDto(Product product) {
    ProductDto productDto = modelMapper.map(product, ProductDto.class);
    List<Image> images = imageRepository.findByProductId(product.getId());
    List<ImageDto> imageDtos = images.stream()
            .map(image -> modelMapper.map(image, ImageDto.class))
            .toList();
    productDto.setImages(imageDtos);
    return productDto;
  }
}
