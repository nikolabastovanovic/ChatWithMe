package com.example.johny.chatwithme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserId, currentUsername, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDB();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields()
    {
        mToolBar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_messages);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
    }

    private void GetUserInfo()
    {
        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUsername = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageInfoToDB()
    {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();
        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar ccalForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(ccalForDate.getTime());

            Calendar ccalForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(ccalForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUsername);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "   " + chatDate + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
