package com.yourname.qrinventory.network;

import com.yourname.qrinventory.model.Product;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductLookup {
    private static final String FIELDS = "product_name,brands,image_url,categories";
    private static final String USER_AGENT = "QRInventoryApp/1.0 (college project)";
    private static OpenFoodFactsApi api;

    // result arrives on the main thread; error == null means success
    public interface LookupCallback { void onResult(Product product, String error); }

    private static OpenFoodFactsApi api() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl("https://world.openfoodfacts.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(OpenFoodFactsApi.class);
        }
        return api;
    }

    // Returns a Product with qrCode, name, category and imageUrl filled in.
    // Price and stock are left at 0 for the admin to enter.
    public static void lookup(String barcode, LookupCallback cb) {
        api().getProduct(barcode, FIELDS, USER_AGENT).enqueue(new Callback<OffResponse>() {
            @Override public void onResponse(Call<OffResponse> call, Response<OffResponse> resp) {
                OffResponse body = resp.body();
                if (!resp.isSuccessful() || body == null || body.status != 1 || body.product == null) {
                    cb.onResult(null, "Product not found in Open Food Facts");
                    return;
                }
                Product p = new Product();
                p.qrCode = barcode;
                String name = body.product.product_name;
                if (name == null || name.trim().isEmpty()) {
                    name = body.product.brands != null ? body.product.brands : "Unnamed product";
                }
                p.name = name.trim();
                String cat = "Grocery";
                if (body.product.categories != null && !body.product.categories.trim().isEmpty()) {
                    cat = body.product.categories.split(",")[0].trim();
                }
                p.category = cat;
                p.imageUrl = body.product.image_url;
                p.lowStockThreshold = 5;
                cb.onResult(p, null);
            }

            @Override public void onFailure(Call<OffResponse> call, Throwable t) {
                cb.onResult(null, "No internet or server unreachable");
            }
        });
    }
}
