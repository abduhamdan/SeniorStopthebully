package com.SeniorBullyingApp.tabview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.SeniorBullyingApp.tabview.Adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Models.AllMethods;
import Models.Message;
import Models.User;


public class ChatFragment extends Fragment implements View.OnClickListener{


FirebaseAuth fAuth;
FirebaseDatabase fb;
DatabaseReference dr;
MessageAdapter mA;
List<Message> listofMessages;
RecyclerView RVMessage;
EditText textEntry;
Button bt;
User a;
ProgressBar PBChat;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

       View v= inflater.inflate(R.layout.fragment_chat, container, false);
       init(v);






        return v;
    }

    private void init(View v) {

        fAuth=FirebaseAuth.getInstance();
        fb= FirebaseDatabase.getInstance();
        a=new User();
        RVMessage=v.findViewById(R.id.recyclerView);
        textEntry=v.findViewById(R.id.editTextTextPersonName);
        bt=v.findViewById(R.id.sendMessage);
        bt.setOnClickListener(this);
        listofMessages=new ArrayList<>();
        PBChat= v.findViewById(R.id.PBChat);

    }

    @Override
    public void onClick(View view) {
        if (!TextUtils.isEmpty(textEntry.getText().toString()))
        {
            Message x = new Message(textEntry.getText().toString(), a.getTwitteru());
            textEntry.setText("");
            dr.push().setValue(x);
        }
        else
            {
               //error
            }
    }


    @Override
    public void onResume() {
        super.onResume();
        listofMessages=new ArrayList<>();

    }

    public void onStart() {

        super.onStart();
        final FirebaseUser CU = fAuth.getCurrentUser();
        a.setEma(CU.getEmail());

        fb.getReference("Users").child(CU.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                a=dataSnapshot.getValue(User.class);
                AllMethods.username=a.getTwitteru();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        dr=fb.getReference("messages");
        dr.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message m=dataSnapshot.getValue(Message.class);
            m.setKey(dataSnapshot.getKey());
            listofMessages.add(m);
            displayMessages(listofMessages);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message m= dataSnapshot.getValue(Message.class);
                m.setKey(dataSnapshot.getKey());
                List<Message> newlyadded = new ArrayList<Message>();
                for (Message w: listofMessages)
                {
                    if(w.getKey().equals(m.getKey()))
                    {
                        newlyadded.add(m);
                    }
                    else

                    {
                        newlyadded.add(w);
                    }
                }
                listofMessages=newlyadded;
                displayMessages(listofMessages);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Message m= dataSnapshot.getValue(Message.class);
                m.setKey(dataSnapshot.getKey());
                List<Message> nMessages = new ArrayList<Message>();
                for (Message w: listofMessages)
                {
                    if(!w.getKey().equals(m.getKey()))
                    {
                        nMessages.add(w);
                    }
                }
                listofMessages=nMessages;
                displayMessages(listofMessages);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void displayMessages(List<Message> listofMessages) {
        PBChat.setVisibility(View.VISIBLE);
        RVMessage.setLayoutManager(new LinearLayoutManager(getContext()));
        mA= new MessageAdapter(getContext(), listofMessages,dr);
        RVMessage.setAdapter(mA);
        PBChat.setVisibility(View.INVISIBLE);
    }
}