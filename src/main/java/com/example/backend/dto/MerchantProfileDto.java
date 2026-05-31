package com.example.backend.dto;

public class MerchantProfileDto {
    private String shopName;
    private String addressLocation;

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getAddressLocation() { return addressLocation; }
    public void setAddressLocation(String addressLocation) { this.addressLocation = addressLocation; }
}
