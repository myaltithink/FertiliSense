package com.fertilisense.fertilisense;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.picasso.Picasso; // Import for Picasso
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messageList;

    // Constants for view types
    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_BOT = 1;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_USER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_sender_view, parent, false);
                return new UserView(view);
            case VIEW_TYPE_BOT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_reciever_view, parent, false);
                return new ReceiverView(view);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        Log.d("MessageAdapter", "Loading image from URL: " + message.getUserImage());

        if (getItemViewType(position) == VIEW_TYPE_USER) {
            UserView userHolder = (UserView) holder;
            userHolder.senderText.setText(message.getMessage());

            // Load user image using Picasso
            Uri userImageUrl = message.getUserImage();
            if (userImageUrl != null) {
                Picasso.with(userHolder.userImage.getContext()) // Use Picasso.with() instead of Picasso.get()
                        .load(userImageUrl)
                        .placeholder(R.drawable.ic_user) // Placeholder image while loading
                        .error(R.drawable.ic_user) // Error image in case of failure
                        .into(userHolder.userImage);
            } else {
                userHolder.userImage.setImageResource(R.drawable.ic_user); // Default placeholder
            }
        } else {
            ReceiverView receiverHolder = (ReceiverView) holder;
            receiverHolder.receiverText.setText(message.getMessage());
            // Handle receiver view as needed
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Return different view types based on who sent the message
        Message message = messageList.get(position);
        Log.d("MessageAdapter", "Binding message at position: " + position);

        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            return VIEW_TYPE_USER; // Sent by user (sender)
        } else {
            return VIEW_TYPE_BOT; // Sent by bot (receiver)
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class UserView extends RecyclerView.ViewHolder {
        TextView senderText;
        CircleImageView userImage; // Add CircleImageView reference

        public UserView(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.sender_text);
            userImage = itemView.findViewById(R.id.user_image); // Initialize userImage
        }
    }

    // ReceiverView ViewHolder for bot
    public static class ReceiverView extends RecyclerView.ViewHolder {
        TextView receiverText;

        public ReceiverView(@NonNull View itemView) {
            super(itemView);
            receiverText = itemView.findViewById(R.id.receiver_text);
        }
    }
}
