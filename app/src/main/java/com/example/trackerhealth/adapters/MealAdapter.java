package com.example.trackerhealth.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackerhealth.R;
import com.example.trackerhealth.model.Meal;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
    
    private final List<Meal> mealList;
    private final Context context;
    private final OnMealClickListener listener;

    public MealAdapter(Context context, List<Meal> mealList, OnMealClickListener listener) {
        this.context = context;
        this.mealList = mealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        
        // Set meal name
        holder.tvMealName.setText(meal.getName());
        
        // Set meal type (breakfast, lunch, dinner, etc)
        holder.tvMealType.setText(meal.getMealType());
        
        // Set calories
        holder.tvCalories.setText(String.format("%d kcal", meal.getCalories()));
        
        // Set time
        holder.tvTime.setText(meal.getTime());

        // Set nutritional information
        holder.tvProtein.setText(String.format("%.1fg P", meal.getProteins()));
        holder.tvCarbs.setText(String.format("%.1fg C", meal.getCarbs()));
        holder.tvFats.setText(String.format("%.1fg F", meal.getFats()));
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName;
        TextView tvMealType;
        TextView tvCalories;
        TextView tvTime;
        TextView tvProtein;
        TextView tvCarbs;
        TextView tvFats;
        ImageView ivMealIcon;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvMealType = itemView.findViewById(R.id.tv_meal_type);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvProtein = itemView.findViewById(R.id.tv_protein);
            tvCarbs = itemView.findViewById(R.id.tv_carbs);
            tvFats = itemView.findViewById(R.id.tv_fats);
            ivMealIcon = itemView.findViewById(R.id.iv_meal_icon);
        }
    }

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
    }
} 