package com.yourname.qrinventory.network;

public class OffResponse {
    public int status;
    public OffProduct product;

    public static class OffProduct {
        public String product_name;
        public String brands;
        public String image_url;
        public String categories;
    }
}
