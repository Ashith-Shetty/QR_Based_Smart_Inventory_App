package com.yourname.qrinventory.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.yourname.qrinventory.data.InventoryRepository;
import com.yourname.qrinventory.model.CategoryStock;
import com.yourname.qrinventory.model.DailySales;
import com.yourname.qrinventory.model.Order;
import com.yourname.qrinventory.model.TopSeller;
import java.util.List;

public class ReportViewModel extends AndroidViewModel {
    public final LiveData<List<CategoryStock>> stockByCategory;   // pie chart
    public final LiveData<List<TopSeller>> topSellers;            // bar chart
    public final LiveData<List<DailySales>> dailySales;           // line chart
    public final LiveData<List<Order>> orders;                    // order history

    public ReportViewModel(@NonNull Application app) {
        super(app);
        InventoryRepository repo = new InventoryRepository(app);
        stockByCategory = repo.getStockByCategory();
        topSellers = repo.getTopSellers();
        dailySales = repo.getDailySales();
        orders = repo.getOrders();
    }
}