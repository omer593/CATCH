package com.example.acatch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, distance;
        ImageView userImage;

        public ViewHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            distance = itemView.findViewById(R.id.userDistance);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);

        // 🔥 שם בטוח (בלי כפילויות!)
        if (user.name != null && !user.name.isEmpty()) {
            holder.userName.setText(user.name);
        } else {
            holder.userName.setText("Unknown");
        }

        // 🔥 מרחק בטוח
        holder.distance.setText("Distance: " +
                String.format("%.2f", user.distance) + " km");

        // 🔥 תמונה בטוחה (מונע קריסה)
        try {
            if (user.imageUrl != null && !user.imageUrl.isEmpty()) {
                Picasso.get()
                        .load(user.imageUrl)
                        .placeholder(R.drawable.circle_bg)
                        .error(R.drawable.circle_bg)
                        .into(holder.userImage);
            } else {
                holder.userImage.setImageResource(R.drawable.circle_bg);
            }
        } catch (Exception e) {
            holder.userImage.setImageResource(R.drawable.circle_bg);
        }

        // 🔥 לחיצה
        holder.itemView.setOnClickListener(v -> {

            android.content.Intent intent =
                    new android.content.Intent(v.getContext(), UserProfileActivity.class);

            intent.putExtra("email", user.email);
            intent.putExtra("userId", user.id);
            intent.putExtra("distance", user.distance);
            intent.putExtra("lat", user.lat);
            intent.putExtra("lng", user.lng);
            intent.putExtra("name", user.name);
            intent.putExtra("instagram", user.instagram);
            intent.putExtra("facebook", user.facebook);
            intent.putExtra("linkedin", user.linkedin);
            intent.putExtra("twitter", user.twitter);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}