package com.example.medilink.Chats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.medilink.R;

public class ChatFragment extends Fragment {
    TextView tvResponse;
    EditText etMessage;
    ImageButton btnSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        init(v);

        btnSend.setOnClickListener(view -> {
            String userMessage = etMessage.getText().toString().trim();
            if(!userMessage.isEmpty()){
                // Overwrite chat with user message
                String botResponse = getBotResponse(userMessage);

                tvResponse.setText("You: " + userMessage + "\n\nBot: " + botResponse);
                etMessage.setText("");
            }
        });
        
        return v;
    }

    private void init(View view) {
        tvResponse = view.findViewById(R.id.tvResponse);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
    }

    private String getBotResponse(String question) {
        return "This is the bot's answer to: \"" + question + "\"";
    }
}