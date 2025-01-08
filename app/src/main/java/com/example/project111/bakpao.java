package com.example.project111;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class bakpao extends AppCompatActivity {

    private RecyclerView rvComments;
    private EditText etComment;
    private Button btnSubmitComment;
    private ArrayList<Comment> commentList;
    private CommentAdapter commentAdapter;
    private DatabaseReference databaseReference;
    private String parentCommentId = null; // Untuk balasan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bakpao);

        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        btnSubmitComment = findViewById(R.id.btnSubmitComment);

        databaseReference = FirebaseDatabase.getInstance().getReference("comments");
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, this::replyToComment);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        btnSubmitComment.setOnClickListener(v -> addCommentToFirebase());
        loadCommentsFromFirebase();
    }

    private void addCommentToFirebase() {
        String commentText = etComment.getText().toString().trim();
        if (!TextUtils.isEmpty(commentText)) {
            SharedPreferences preferences = getSharedPreferences("USER_DATA", MODE_PRIVATE);
            String username = preferences.getString("username", "Anonymous");

            String commentId = databaseReference.push().getKey();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            Comment comment = new Comment(commentId, username, commentText, timestamp, parentCommentId);

            if (parentCommentId == null) {
                databaseReference.child(commentId).setValue(comment);
            } else {
                databaseReference.child(parentCommentId).child("replies").child(commentId).setValue(comment);
                parentCommentId = null;
            }

            etComment.setText("");
            etComment.setHint("Tambahkan komentar...");
            Toast.makeText(this, "Komentar dikirim!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCommentsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(bakpao.this, "Gagal memuat komentar!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void replyToComment(String commentId) {
        parentCommentId = commentId;
        etComment.setHint("Balas komentar...");
    }
}
