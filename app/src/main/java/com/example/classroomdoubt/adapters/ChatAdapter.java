package com.example.classroomdoubt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.models.ChatMessage;
import com.example.classroomdoubt.utils.TimeUtil;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private List<ChatMessage> messages;
    private final String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> messages,
                       String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        }
        return VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof SentViewHolder) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.tvMessage.setText(message.getMessage());
            sentHolder.tvTime.setText(TimeUtil.getTimeAgo(message.getTimestamp()));

        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;
            receivedHolder.tvMessage.setText(message.getMessage());
            receivedHolder.tvTime.setText(
                    TimeUtil.getTimeAgo(message.getTimestamp()));

            // Show sender name + role badge
            String role = message.getSenderRole();
            String badge = "faculty".equals(role) ? " 👨‍🏫" : " 🎓";
            receivedHolder.tvSenderName.setText(
                    message.getSenderName() + badge);

            // Different color for faculty messages
            if ("faculty".equals(role)) {
                receivedHolder.tvSenderName.setTextColor(
                        context.getResources().getColor(
                                R.color.faculty_primary));
            } else {
                receivedHolder.tvSenderName.setTextColor(
                        context.getResources().getColor(
                                R.color.student_primary));
            }
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    public void updateList(List<ChatMessage> newList) {
        this.messages = newList;
        notifyDataSetChanged();
    }

    // Sent message ViewHolder
    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    // Received message ViewHolder
    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSenderName;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }
    }
}