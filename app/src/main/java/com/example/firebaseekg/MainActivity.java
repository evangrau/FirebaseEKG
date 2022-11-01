package com.example.firebaseekg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Button registerButton, loginButton, logoutButton;
    private FirebaseListAdapter<ChatMessage> adapter;

    FirebaseUser currentUser;

    ConstraintLayout noUserPanel;
    RelativeLayout chatLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noUserPanel = (ConstraintLayout) findViewById(R.id.noUserPanel);
        chatLayout = (RelativeLayout) findViewById(R.id.chatMessage);

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                if (input.getText().toString().isEmpty()) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Message cannot be empty!",
                            Toast.LENGTH_SHORT);
                }
                else {
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getDisplayName())
                            );

                    // Clear the input
                    input.setText("");
                }
            }
        });

        registerButton = (Button) findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        logoutButton = (Button) findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // logs out the user
                FirebaseAuth.getInstance().signOut();

                // recreates the activity so that the user sees the original screen
                recreate();
            }
        });

        // setting the layouts to gone to start
        noUserPanel.setVisibility(View.GONE);
        chatLayout.setVisibility(View.GONE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null)
        {
            noUserPanel.setVisibility(View.VISIBLE);
        }
        else
        {
            chatLayout.setVisibility(View.VISIBLE);
            displayChatMessages();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null) {
            adapter.stopListening();
        }
    }

    private void displayChatMessages() {
        ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(FirebaseDatabase.getInstance().getReference(), ChatMessage.class)
                .setLayout(R.layout.message).build();

        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
}