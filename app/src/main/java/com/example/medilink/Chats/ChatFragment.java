package com.example.medilink.Chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import com.example.medilink.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatFragment extends Fragment {

    private TextView tvResponse;
    private EditText etMessage;
    private ImageButton btnSend;

    private static final String GEMINI_API_KEY = "hello";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        init(v);

        btnSend.setOnClickListener(view -> {
            String userMessage = etMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
//                sendMessageToBot(userMessage);
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

    private void sendMessageToBot(String userMessage) {
        // Show user message immediately
        tvResponse.setText("You: " + userMessage + "\n\nBot: ...thinking...");

        try {
            // Prepare JSON payload for Gemini API
            JSONObject inputJson = new JSONObject();
            JSONArray inputArray = new JSONArray();
            JSONObject messageJson = new JSONObject();
            messageJson.put("content", userMessage);
            messageJson.put("type", "text");
            inputArray.put(messageJson);
            inputJson.put("input", inputArray);

            RequestBody body = RequestBody.create(
                    inputJson.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://api.generativeai.googleapis.com/v1beta2/models/gemini-2.5-flash:generateMessage")
                    .addHeader("Authorization", "Bearer " + GEMINI_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "API Error: " + response.code(), Toast.LENGTH_LONG).show()
                        );
                        return;
                    }

                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray outputArray = json.getJSONArray("output");
                        String botReply = outputArray.getJSONObject(0)
                                .getJSONObject("content")
                                .getString("text");

                        requireActivity().runOnUiThread(() ->
                                tvResponse.setText("You: " + userMessage + "\n\nBot: " + botReply)
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Parsing Error", Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
