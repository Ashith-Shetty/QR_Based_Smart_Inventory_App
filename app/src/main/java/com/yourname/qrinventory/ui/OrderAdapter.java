package com.yourname.qrinventory.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yourname.qrinventory.R;
import com.yourname.qrinventory.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface OnOrderClick { void onClick(Order order); }

    private final List<Order> items = new ArrayList<>();
    private final OnOrderClick listener;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public OrderAdapter(OnOrderClick listener) { this.listener = listener; }

    public void setItems(List<Order> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order o = items.get(position);
        h.title.setText("Order #" + o.id);
        h.meta.setText(fmt.format(new Date(o.timestamp))
                + "  |  " + String.format(Locale.getDefault(), "₹%.2f", o.total));
        h.itemView.setOnClickListener(v -> listener.onClick(o));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, meta;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tv_order_title);
            meta = v.findViewById(R.id.tv_order_meta);
        }
    }
}