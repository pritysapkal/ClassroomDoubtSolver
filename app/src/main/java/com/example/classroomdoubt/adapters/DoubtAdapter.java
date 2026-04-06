package com.example.classroomdoubt.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.activities.DoubtDetailActivity;
import com.example.classroomdoubt.models.Doubt;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.TimeUtil;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DoubtAdapter extends RecyclerView.Adapter<DoubtAdapter.DoubtViewHolder> {

    private final Context context;
    private List<Doubt> doubts;
    private final String userRole;
    private final String currentUserId;

    public DoubtAdapter(Context context, List<Doubt> doubts, String userRole, String currentUserId) {
        this.context = context;
        this.doubts = doubts;
        this.userRole = userRole;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public DoubtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doubt, parent, false);
        return new DoubtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoubtViewHolder holder, int position) {
        Doubt doubt = doubts.get(position);

        holder.tvTitle.setText(doubt.getTitle());
        holder.tvDescription.setText(doubt.getDescription());
        holder.tvDescription.setText(doubt.getDescription());
        holder.tvAuthor.setText("👤 " + doubt.getAuthorName());
        holder.tvTime.setText(TimeUtil.getTimeAgo(doubt.getCreatedAt()));
        holder.btnUpvote.setText("👍 " + doubt.getUpvotes());

        // Privacy indicator
        if (doubt.isPrivate()) {
            holder.tvIsPrivate.setVisibility(View.VISIBLE);
        } else {
            holder.tvIsPrivate.setVisibility(View.GONE);
        }

        // Attachment icon
        if (doubt.getAttachmentUrl() != null && !doubt.getAttachmentUrl().isEmpty()) {
            holder.tvHasAttachment.setVisibility(View.VISIBLE);
        } else {
            holder.tvHasAttachment.setVisibility(View.GONE);
        }

        // Status badge
        updateStatusBadge(holder, doubt.getStatus());

        // Priority indicator in title
        String priorityIcon = "medium".equals(doubt.getPriority()) ? "🟡 "
                : "high".equals(doubt.getPriority()) ? "🔴 " : "🟢 ";
        holder.tvTitle.setText(priorityIcon + doubt.getTitle());

        // Upvote checking
        checkUpvoteStatus(holder, doubt.getDoubtId());

        holder.btnUpvote.setOnClickListener(v -> toggleUpvote(doubt.getDoubtId(), doubt.getUpvotes()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DoubtDetailActivity.class);
            intent.putExtra(DoubtDetailActivity.EXTRA_DOUBT_ID, doubt.getDoubtId());
            intent.putExtra(DoubtDetailActivity.EXTRA_DOUBT_TITLE, doubt.getTitle());
            context.startActivity(intent);
        });
    }

    private void updateStatusBadge(DoubtViewHolder holder, String status) {
        if ("resolved".equals(status)) {
            holder.tvStatus.setText("RESOLVED");
            holder.tvStatus.setBackgroundResource(R.drawable.badge_green);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.faculty_primary));
        } else if ("answered".equals(status)) {
            holder.tvStatus.setText("ANSWERED");
            holder.tvStatus.setBackgroundResource(R.drawable.badge_green);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.faculty_primary));
        } else {
            holder.tvStatus.setText("PENDING");
            holder.tvStatus.setBackgroundResource(R.drawable.badge_orange);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_open));
        }
    }

    private void checkUpvoteStatus(DoubtViewHolder holder, String doubtId) {
        FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES)
                .child(doubtId).child(currentUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        holder.btnUpvote.setTextColor(context.getResources().getColor(R.color.primary));
                    } else {
                        holder.btnUpvote.setTextColor(context.getResources().getColor(R.color.text_secondary));
                    }
                });
    }

    private void toggleUpvote(String doubtId, int currentCount) {
        String path = doubtId + "/" + currentUserId;
        FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES)
                .child(path).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES)
                                .child(path).removeValue();
                        FirebaseHelper.getDoubtsRef().child(doubtId).child("upvotes")
                                .setValue(Math.max(0, currentCount - 1));
                    } else {
                        FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES)
                                .child(path).setValue(true);
                        FirebaseHelper.getDoubtsRef().child(doubtId).child("upvotes")
                                .setValue(currentCount + 1);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return doubts.size();
    }

    public void updateList(List<Doubt> newList) {
        this.doubts = newList;
        notifyDataSetChanged();
    }

    static class DoubtViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvAuthor, tvTime, tvHasAttachment, tvIsPrivate;
        MaterialButton btnUpvote;

        DoubtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvHasAttachment = itemView.findViewById(R.id.tvHasAttachment);
            tvIsPrivate = itemView.findViewById(R.id.tvIsPrivate);
            btnUpvote = itemView.findViewById(R.id.btnUpvote);
        }
    }
}