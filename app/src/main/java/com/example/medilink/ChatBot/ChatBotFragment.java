package com.example.medilink.ChatBot;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

public class ChatBotFragment extends Fragment {

    private TextView tvResponse;
    private EditText etMessage;
    private ImageButton btnSend;
    private ScrollView scrollView;

    private static final String GEMINI_API_KEY = "AIzaSyCLlvBuGzPCJ8orFdGZ1Sno-0A0Lo5WlhY";

    private final OkHttpClient client = new OkHttpClient();

    private final SpannableStringBuilder chatHistory = new SpannableStringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chatbot, container, false);

        init(v);

        // Button click
        btnSend.setOnClickListener(view -> sendUserMessage());

        // Enter key press
        etMessage.setOnEditorActionListener((v1, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEND ||
                    actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                sendUserMessage();
                return true;
            }
            return false;
        });

        return v;
    }

    private void init(View view) {
        tvResponse = view.findViewById(R.id.tvResponse);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        scrollView = view.findViewById(R.id.scrollView);
    }

    private void sendUserMessage() {
        String userMessage = etMessage.getText().toString().trim();
        if (userMessage.isEmpty()) return;

        appendMessage("You", userMessage);

        etMessage.setText("");
        hideKeyboard();

        sendMessageToBot(userMessage);
    }

    private void appendMessage(String sender, String message) {
        int start = chatHistory.length();

        // Add "You: message" or "Bot: message"
        String line = sender + ": " + message + "\n\n";
        chatHistory.append(line);

        // Set color depending on sender
        int color = (sender.equals("You"))
                ? ContextCompat.getColor(getContext(), R.color.white)  // User messages
                : ContextCompat.getColor(getContext(), R.color.Red); // Bot messages

        chatHistory.setSpan(
                new ForegroundColorSpan(color),
                start,
                chatHistory.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvResponse.setText(chatHistory);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }


    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) requireActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void sendMessageToBot(String userMessage) {
        try {
            JSONObject requestJson = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            contentObj.put("parts",
                    new JSONArray().put(new JSONObject().put("text", userMessage))
            );
            contentsArray.put(contentObj);
            requestJson.put("contents", contentsArray);

            RequestBody body = RequestBody.create(
                    requestJson.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY)
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

                    String respBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(respBody);

                        String botReply = json
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        requireActivity().runOnUiThread(() -> appendMessage("Bot", botReply));

                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Parsing error", Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
