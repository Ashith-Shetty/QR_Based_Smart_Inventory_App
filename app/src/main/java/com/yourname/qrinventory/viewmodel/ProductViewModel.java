package com.yourname.qrinventory.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.yourname.qrinventory.data.InventoryRepository;
import com.yourname.qrinventory.model.Product;
import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private final InventoryRepository repo;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    public final LiveData<List<Product>> products;
    public final LiveData<List<String>> categories;

    public ProductViewModel(@NonNull Application app) {
        super(app);
        repo = new InventoryRepository(app);
        products = Transformations.switchMap(query, q -> repo.searchProducts(q));
        categories = repo.getCategories();
    }

    public void setQuery(String q) { query.setValue(q == null ? "" : q); }   // "" shows everything
    public LiveData<List<Product>> byCategory(String cat) { return repo.productsByCategory(cat); }
    public void add(Product p) { repo.addProduct(p); }
    public void update(Product p) { repo.updateProduct(p); }
    public void delete(Product p) { repo.deleteProduct(p); }
}
