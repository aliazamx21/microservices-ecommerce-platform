package com.order_service.dto;

import java.util.List;

public class CartResponseDTO {
    private String uuid;
    private Long userId;
    private List<CartItemResponseDTO> items;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItemResponseDTO> getItems() { return items; }
    public void setItems(List<CartItemResponseDTO> items) { this.items = items; }
}