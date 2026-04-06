package com.example.classroomdoubt.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.models.Answer;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.TimeUtil;

import java.util.List;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder> {

    private final Context context;
    private List<Answer> answers;
    private final String userRole;
    private final String userId;

    public AnswerAdapter(Context context, List<Answer> answers, String userRole, String userId) {
        this.context = context;
        this.answers = answers;
        this.userRole = userRole;
        this.userId = userId;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_answer, parent, false);
        return new AnswerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        Answer answer = answers.get(position);

        holder.tvFacultyName.setText("Prof. " + answer.getFacultyName());
        holder.tvFacultyDept.setText(answer.getFacultyDept());
        holder.tvAvatar.setText("👨‍🏫");
        holder.cardView.setCardBackgroundColor(Color.WHITE);

        holder.tvAnswerText.setText(answer.getAnswerText());
        holder.tvAnswerTime.setText(TimeUtil.getTimeAgo(answer.getCreatedAt()));

        // Show existing rating
        if (answer.getStudentRating() > 0) {
            holder.ratingStudent.setRating(answer.getStudentRating());
            holder.tvRatingCount.setText((int) answer.getStudentRating() + "/5");
        } else {
            holder.ratingStudent.setRating(0);
            holder.tvRatingCount.setText("");
        }

        // Best answer badge
        holder.tvBestAnswer.setVisibility(answer.isBestAnswer() ? View.VISIBLE : View.GONE);

        // Only STUDENTS can rate answers
        if ("student".equals(userRole)) {
            holder.ratingStudent.setIsIndicator(false);
            holder.ratingStudent.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser && rating > 0) {
                    FirebaseHelper.getAnswersRef().child(answer.getAnswerId()).child("studentRating").setValue(rating)
                            .addOnSuccessListener(unused -> {
                                holder.tvRatingCount.setText((int) rating + "/5");
                                Toast.makeText(context, "Thanks for rating!", Toast.LENGTH_SHORT).show();
                                if (rating == 5) {
                                    FirebaseHelper.getAnswersRef().child(answer.getAnswerId()).child("isBestAnswer").setValue(true);
                                    holder.tvBestAnswer.setVisibility(View.VISIBLE);
                                }
                            });
                }
            });
        } else {
            holder.ratingStudent.setIsIndicator(true);
        }
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    public void updateList(List<Answer> newList) {
        this.answers = newList;
        notifyDataSetChanged();
    }

    static class AnswerViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvFacultyName, tvFacultyDept, tvAnswerText, tvAnswerTime, tvBestAnswer, tvRatingCount, tvAvatar;
        RatingBar ratingStudent;

        AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvFacultyName = itemView.findViewById(R.id.tvFacultyName);
            tvFacultyDept = itemView.findViewById(R.id.tvFacultyDept);
            tvAnswerText = itemView.findViewById(R.id.tvAnswerText);
            tvAnswerTime = itemView.findViewById(R.id.tvAnswerTime);
            tvBestAnswer = itemView.findViewById(R.id.tvBestAnswer);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            ratingStudent = itemView.findViewById(R.id.ratingStudent);
        }
    }
}
