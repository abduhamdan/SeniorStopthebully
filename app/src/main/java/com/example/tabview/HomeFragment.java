package com.example.tabview;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HomeFragment extends Fragment {


private TextView firstname;
private TextView sscore;
private PyObject object;
private PyObject obj;
private TextView weeklyscore, weeklyracial, weeklygender,weeklypersonal, monthlyscore, monthlyracial, monthlygender, monthlypersonal;
private DecimalFormat df = new DecimalFormat();
private ProgressBar loadingvalues;
private double personalscore;
private ProgressBar weeklyPB, monthlyPB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!Python.isStarted()) Python.start(new AndroidPlatform(getContext()));
        // Inflate the layout for this fragment
       View v= inflater.inflate(R.layout.fragment_home, container, false);
        firstname=v.findViewById(R.id.Name);
        sscore=v.findViewById(R.id.sScore);
        weeklyscore=v.findViewById(R.id.weeklybullyingscore);
        weeklyracial=v.findViewById(R.id.weeklyracialtweets);
        weeklygender=v.findViewById(R.id.weeklygendertweets);
        weeklypersonal=v.findViewById(R.id.weeklypersonaltweets);
        monthlygender=v.findViewById(R.id.monthlygendertweets);
        monthlypersonal=v.findViewById(R.id.monthlypersonaltweets);
        monthlyracial=v.findViewById(R.id.monthlyracialtweets);
        monthlyscore=v.findViewById(R.id.monthlybullyingscore);
        weeklyPB=v.findViewById(R.id.weeklybullyingscorePB);
        monthlyPB=v.findViewById(R.id.monthlybullyingscorePB);
        loadingvalues=v.findViewById(R.id.PBforanalysispage);
        loadingvalues.setVisibility(View.VISIBLE);
        displayFirstName();
        getSusceptibilityScore();
        new MyAsyncTask().execute();


        return v;
    }
    public class MyAsyncTask extends AsyncTask<Void, Void, String> { //do this in a thread to make it faster

        @Override
        protected String doInBackground(Void... voids) {
            Python py= Python.getInstance();
            object= py.getModule("tweepy_streamer score");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            getScoreValues();
            super.onPostExecute(s);
        }
    }
    private void getSusceptibilityScore() {

        FirebaseDatabase data=FirebaseDatabase.getInstance();
        DatabaseReference myref=data.getReference("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                User value = dataSnapshot.getValue(User.class); //stores the values in this user
                sscore.setText(value.personalstaticscore.toString());
                personalscore=Float.parseFloat(value.personalstaticscore);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    private void getScoreValues()
    {

        FirebaseDatabase data=FirebaseDatabase.getInstance();
        DatabaseReference myref=data.getReference("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                User value = dataSnapshot.getValue(User.class); //stores the values in this user


//                Python py= Python.getInstance();
//                object= py.getModule("tweepy_streamer score");
               obj = object.callAttr("getMetrics", ""+value.twitteru);

                df.setMaximumFractionDigits(2); //set values to 2 decimal places
                //System.out.println(df.format(decimalNumber));
                double wscore = (personalscore+Math.abs(obj.asList().get(0).toFloat()))/2;
                double mscore = (personalscore+Math.abs(obj.asList().get(1).toFloat()))/2;
                weeklyscore.setText( df.format(wscore)); //each score (personal and tweet) accounts for 50% of the score
                monthlyscore.setText(df.format(mscore));
                Log.d("test1", Math.abs(obj.asList().get(0).toFloat())+"");
                Log.d("test2", personalscore+"");
                Log.d("test3", (int)(mscore*100)+"");
                Log.d("test4",mscore+"" );
                Log.d("test5", wscore+"");
                weeklyPB.setProgress((int)(wscore*100));
                monthlyPB.setProgress((int)(mscore *100));

                weeklyracial.setText(obj.asList().get(2).toString());
                weeklygender.setText(obj.asList().get(3).toString());
                weeklypersonal.setText(obj.asList().get(4).toString());
                monthlyracial.setText(obj.asList().get(5).toString());
                monthlygender.setText(obj.asList().get(6).toString());
                monthlypersonal.setText(obj.asList().get(7).toString());
                loadingvalues.setVisibility(View.GONE);

            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }
    private void displayFirstName() {
        FirebaseDatabase data=FirebaseDatabase.getInstance();
        DatabaseReference myref=data.getReference("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
     myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                User value = dataSnapshot.getValue(User.class); //stores the values in this user
                firstname.setText("Welcome "+ value.getFname() + " " + value.getLname()+"!");
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}