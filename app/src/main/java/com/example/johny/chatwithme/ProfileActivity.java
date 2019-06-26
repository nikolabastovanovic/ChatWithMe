package com.example.johny.chatwithme;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID, currentState, currentUserId;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineRequestButton;

    private DatabaseReference userRef, chatReqRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_activity);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        recieverUserID = getIntent().getExtras().get("visitUserID").toString();
        currentUserId = mAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_username);
        userProfileStatus = (TextView) findViewById(R.id.visit_user_status);
        sendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        declineRequestButton = (Button) findViewById(R.id.decline_message_request_button);
        currentState = "new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        userRef.child(recieverUserID).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void ManageChatRequest()
    {
        //Funkcionalnost koja omogucava da kada se posalje zahtev dugme ostane u statusu za opoziv slanja request-a
        chatReqRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieverUserID))
                {
                    String requestType = dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();

                    if (requestType.equals("sent"))
                    {
                        currentState = "request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }
                    else if (requestType.equals("received"))
                    {
                        currentState = "request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");

                        declineRequestButton.setVisibility(View.VISIBLE);
                        declineRequestButton.setEnabled(true);
                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    contactsRef.child(currentUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(recieverUserID))
                                    {
                                        currentState = "friends";
                                        sendMessageRequestButton.setText("Remove Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!currentUserId.equals(recieverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);

                    if (currentState.equals("new"))
                    {
                        SendChatRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friends"))
                    {
                        RemoveContact();
                    }
                }
            });
        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveContact()
    {
        contactsRef.child(currentUserId).child(recieverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(recieverUserID).child(currentUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState = "new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest()
    {
        //Dodavanje u kontakte ulogovanog user-a
        contactsRef.child(currentUserId).child(recieverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            //Dodavanje u kontakte prijemnika zahteva
                            contactsRef.child(recieverUserID).child(currentUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                //brisanje iz request-a ulogovanog user-a
                                                chatReqRef.child(currentUserId).child(recieverUserID).removeValue()
                                                        .addOnCompleteListener((new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    ////brisanje iz request-a prijemnika yahteva
                                                                    chatReqRef.child(recieverUserID).child(currentUserId).removeValue()
                                                                            .addOnCompleteListener((new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    currentState = "friends";
                                                                                    sendMessageRequestButton.setText("Remove Contact");

                                                                                    declineRequestButton.setVisibility(View.INVISIBLE);
                                                                                    declineRequestButton.setEnabled(false);
                                                                                }
                                                                            }));
                                                                }
                                                            }
                                                        }));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest()
    {
        chatReqRef.child(currentUserId).child(recieverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatReqRef.child(recieverUserID).child(currentUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState = "new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest()
    {
        chatReqRef.child(currentUserId).child(recieverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            chatReqRef.child(recieverUserID).child(currentUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", currentUserId);
                                                chatNotificationMap.put("type", "request");
                                                notificationRef.child(recieverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}