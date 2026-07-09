package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.CartItemDTO;
import com.lichuan.tea.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public Result<String> addToCart(@RequestParam String userId, @RequestBody CartItemDTO item) {
        cartService.addToCart(userId, item);
        return Result.success("Added to cart");
    }

    @DeleteMapping("/remove")
    public Result<String> removeFromCart(@RequestParam String userId, @RequestParam Long productId, @RequestParam String spec) {
        cartService.removeFromCart(userId, productId, spec);
        return Result.success("Removed from cart");
    }

    @GetMapping
    public Result<List<CartItemDTO>> getCart(@RequestParam String userId) {
        return Result.success(cartService.getCart(userId));
    }

    @DeleteMapping("/clear")
    public Result<String> clearCart(@RequestParam String userId) {
        cartService.clearCart(userId);
        return Result.success("Cart cleared");
    }
}
