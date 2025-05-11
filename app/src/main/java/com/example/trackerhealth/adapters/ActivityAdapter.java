package com.example.trackerhealth.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackerhealth.R;
import com.example.trackerhealth.model.PhysicalActivity;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    
    private final List<PhysicalActivity> activityList;
    private final Context context;
    private final OnActivityActionListener listener;

    public ActivityAdapter(Context context, List<PhysicalActivity> activityList, OnActivityActionListener listener) {
        this.context = context;
        this.activityList = activityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        PhysicalActivity activity = activityList.get(position);
        
        // Set activity type
        holder.tvActivityType.setText(activity.getActivityType());
        
        // Set distance if available
        if (activity.getDistance() > 0) {
            holder.tvDistance.setText(String.format("%.2f km", activity.getDistance()));
            holder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }
        
        // Set duration
        holder.tvDuration.setText(String.format("%d min", activity.getDuration()));
        
        // Set calories
        if (activity.getCaloriesBurned() > 0) {
            holder.tvCalories.setText(String.format("%d kcal", activity.getCaloriesBurned()));
            holder.tvCalories.setVisibility(View.VISIBLE);
        } else {
            holder.tvCalories.setVisibility(View.GONE);
        }
        
        // Set date
        holder.tvDate.setText(activity.getDate());
        
        // Set long click listener for context menu
        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, activity);
            return true;
        });
        
        // Set click listener for view details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActivityClick(activity);
            }
        });
    }

    private void showPopupMenu(View view, PhysicalActivity activity) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.menu_activity_item);
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                if (listener != null) {
                    listener.onEditActivity(activity);
                }
                return true;
            } else if (itemId == R.id.menu_delete) {
                if (listener != null) {
                    listener.onDeleteActivity(activity);
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityType;
        TextView tvDistance;
        TextView tvDuration;
        TextView tvCalories;
        TextView tvDate;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityType = itemView.findViewById(R.id.tv_activity_type);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }

    public interface OnActivityActionListener {
        void onActivityClick(PhysicalActivity activity);
        void onEditActivity(PhysicalActivity activity);
        void onDeleteActivity(PhysicalActivity activity);
    }
} 