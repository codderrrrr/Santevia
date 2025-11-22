package com.example.medilink.BookedAppointment;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.ChatMessages;
import com.example.medilink.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessages> messages = new ArrayList<>();
    private EditText editTextMessage;
    private ImageView buttonSend;

    private FirebaseFirestore db;

    private String senderID;
    private String receiverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        senderID = getIntent().getStringExtra("senderID");
        receiverID = getIntent().getStringExtra("receiverID");

        if (senderID == null || receiverID == null) {
            Toast.makeText(this, "Invalid user IDs", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatAdapter = new ChatAdapter(messages, senderID);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        listenForMessages();

        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private String getChatId() {
        return senderID.compareTo(receiverID) < 0
                ? senderID + "_" + receiverID
                : receiverID + "_" + senderID;
    }

    private void listenForMessages() {
        db.collection("Chats")
                .document(getChatId())
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    messages.clear();

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessages msg = doc.toObject(ChatMessages.class);
                            if (msg != null) messages.add(msg);
                        }

                        chatAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        ChatMessages message = new ChatMessages(
                senderID,
                receiverID,
                text,
                System.currentTimeMillis()
        );

        db.collection("Chats")
                .document(getChatId())
                .collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> editTextMessage.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
