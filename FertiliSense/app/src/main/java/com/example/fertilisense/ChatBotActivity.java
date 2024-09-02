package com.example.fertilisense;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot); // Ensure this matches your XML layout file name

        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        // Setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            if (!question.isEmpty()) {
                addToChat(question, Message.SENT_BY_ME);
                messageEditText.setText("");
                callAPI(question);
                welcomeTextView.setVisibility(View.GONE);
            }
        });
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    void addResponse(String response) {
        // Removing "Typing..." message if present
        int index = messageList.size() - 1;
        if (index >= 0 && Message.SENT_BY_BOT.equals(messageList.get(index).getSentBy()) && "Typing...".equals(messageList.get(index).getMessage())) {
            messageList.remove(index);
        }
        addToChat(response, Message.SENT_BY_BOT);
    }

    void callAPI(String question) {
        // Show typing indicator
        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("sender", "");
            jsonBody.put("message", question);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url("https://cb36-2001-4452-409-cc00-59b6-9f20-6d8f-63fb.ngrok-free.app/webhooks/rest/webhook")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Initialize Gson object
                        Gson gson = new Gson();

                        // Identify the "data type" for conversion
                        Type dataType = new TypeToken<ArrayList<ResponseModel>>() {}.getType();

                        // Convert the HTTP response into a list of ResponseModel (local class)
                        List<ResponseModel> data = gson.fromJson(response.body().string(), dataType);

                        // Display the response text/image
                        // Iterate through the items of list (for-each)
                        for (ResponseModel item : data) {
                            // Check if the current response is an image
                            if (item.getText() == null) {
                                addResponse(item.getImage());
                            } else {
                                addResponse(item.getText());
                            }
                        }
                    } catch (Exception e) {
                        addResponse("Failed to load response due to " + e.getMessage());
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().toString());
                }
            }
        });
    }
}
