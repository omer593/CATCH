package com.example.acatch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userId, distance;

        public ViewHolder(View itemView) {
            super(itemView);
            userId = itemView.findViewById(R.id.userId);
            distance = itemView.findViewById(R.id.distance);
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

        holder.userId.setText(user.email);
        holder.distance.setText("Distance: " + String.format("%.2f", user.distance) + " km");

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
