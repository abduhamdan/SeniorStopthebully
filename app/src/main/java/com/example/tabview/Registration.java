package com.example.tabview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Registration extends Activity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private EditText fname, lname, tusername;
    private Button signupconfirm;
    private ProgressBar PB;
    private Spinner physicalS;
    private Spinner ageS, sexualityS, religionS, genderS, mentalhealthS, countryS, bulliedbeforeS, frequencyS, targetareaS,childgradesS, childmoodS, digitalsecrecyS, avoidanceS;
    private float suscscoreforuser;
    private PyObject obj;
    private PyObject object;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!Python.isStarted()) Python.start(new AndroidPlatform(this));
        Python py= Python.getInstance();
        object= py.getModule("script");

        setContentView(R.layout.activity_registration);
        physicalS=findViewById(R.id.healthSpinner);
        fname=findViewById(R.id.firstNameEditText);
        lname=findViewById(R.id.lastNameEditText);
        tusername=findViewById(R.id.twitterUserNameEditText);
        sexualityS=findViewById(R.id.sexualitySpinner);
        ageS=findViewById(R.id.ageSpinner);
        religionS=findViewById(R.id.religionSpinner);
        genderS=findViewById(R.id.genderSpinner);
        mentalhealthS=findViewById(R.id.mentalSpinner);
        countryS=findViewById(R.id.countrySpinner);
        bulliedbeforeS=findViewById(R.id.bulliedbeforeSpinner);
        frequencyS=findViewById(R.id.frequencySpinner);
        targetareaS=findViewById(R.id.targetareaSpinner);
        childgradesS=findViewById(R.id.childgradesSpinner);
        childmoodS=findViewById(R.id.childmoodSpinner);
        digitalsecrecyS=findViewById(R.id.digitalsecrecySpinner);
        avoidanceS=findViewById(R.id.avoidanceSpinner);
      email=(EditText) findViewById(R.id.editTextTextEmailAddress);
      password=(EditText)findViewById(R.id.editTextTextPassword);
        mAuth = (FirebaseAuth) FirebaseAuth.getInstance();
        // Enables Always-on
    signupconfirm= (Button) findViewById(R.id.buttonxm);
    signupconfirm.setOnClickListener(this);
    PB=(ProgressBar) findViewById(R.id.registrationProgress);




    }


    private void registerUser()
    {
        final String physicalstruggles= physicalS.getSelectedItem().toString();
        final String twitteru=tusername.getText().toString();
        final String firstn=fname.getText().toString();
        final String lastn= lname.getText().toString();
        final String ema = email.getText().toString();
        String pass = password.getText().toString();
        final String age = ageS.getSelectedItem().toString();
        final String religion= religionS.getSelectedItem().toString();
        final String gender = genderS.getSelectedItem().toString();
        final String mentalheathhistory= mentalhealthS.getSelectedItem().toString();
        final String country=countryS.getSelectedItem().toString();
        final String frequency=frequencyS.getSelectedItem().toString();
        final String targetarea=targetareaS.getSelectedItem().toString();
        final String childgrades=childgradesS.getSelectedItem().toString();
        final String childmood=childmoodS.getSelectedItem().toString();
        final String digitalsecrecy=digitalsecrecyS.getSelectedItem().toString();
        final String avoidance=avoidanceS.getSelectedItem().toString();
        final String sexuality=sexualityS.getSelectedItem().toString();
        final String bulliedbefore=bulliedbeforeS.getSelectedItem().toString();

        obj=object.callAttr("check_user_validity", ""+twitteru);
        if(firstn.isEmpty() || !isAlpha(firstn)) //checks if either empty of contains invalid characters
        {
            if(firstn.isEmpty())
            {
                fname.setError("First name is required");
                fname.requestFocus();
                return;
            }
            if(!isAlpha(firstn))
            {
                fname.setError("Invalid First name");
                fname.requestFocus();
                return;
            }
        }


        if(lastn.isEmpty() || !isAlpha(lastn)) //checks if either empty of contains invalid characters
        {
            if(lastn.isEmpty())
            {
                lname.setError("First name is required");
                lname.requestFocus();
                return;
            }
            if(!isAlpha(lastn))
            {
                lname.setError("Invalid First name");
                lname.requestFocus();
                return;
            }
        }
        if (ema.isEmpty()){
            email.setError("Email is required");
            email.requestFocus();
            return;
        }

               if(twitteru.isEmpty()) //need to enter a username
        {
            tusername.setError("Twitter Username is Required");
            tusername.requestFocus();
            return;
        }
               ;
int x= obj.toInt();
               System.out.println(obj);
        if(x==0) //check if valid
        {
            tusername.setError("Twitter Username is invalid");
            tusername.requestFocus();
            return;
        }



        if(age.contains("Select"))
        {

            TextView ageT= findViewById(R.id.agetext);
            ageT.setError("Please select an age group");
            ageT.requestFocus();
            ageT.setTextColor(0xFFFF0000);
            return;
        }


        if(physicalstruggles.contains("Select"))
        {

            TextView physical= findViewById(R.id.healthtext);
            physical.setError("Please select an option below");
            physical.requestFocus();
            physical.setTextColor(0xFFFF0000);
            return;
        }

        if(sexuality.contains("Select"))
        {

            TextView sexualityT= findViewById(R.id.sexualtext);
            sexualityT.setError("Please select an age group");
            sexualityT.requestFocus();
            sexualityT.setTextColor(0xFFFF0000);
            return;
        }

        if(religion.contains("Select"))
        {

            TextView religionT= findViewById(R.id.religiontext);
            religionT.setError("Please select an religion");
            religionT.requestFocus();
            religionT.setTextColor(0xFFFF0000);
            return;
        }

        if(gender.contains("Select"))
        {

            TextView genderT= findViewById(R.id.gendertext);
            genderT.setError("Please select a gender");
            genderT.requestFocus();
            genderT.setTextColor(0xFFFF0000);
            return;
        }

        if(mentalheathhistory.contains("Select"))
        {

            TextView mentalT= findViewById(R.id.mentaltext);
            mentalT.setError("Please select one of the options below");
            mentalT.requestFocus();
            mentalT.setTextColor(0xFFFF0000);
            return;
        }

        if(country.contains("Select"))
        {

            TextView countryT= findViewById(R.id.locationtext);
            countryT.setError("Please select your location");
            countryT.requestFocus();
            countryT.setTextColor(0xFFFF0000);
            return;
        }

        if(bulliedbefore.contains("Select"))
        {

            TextView bulliedT= findViewById(R.id.bulliedbeforetext);
            bulliedT.setError("Please select one of the options below");
            bulliedT.requestFocus();
            bulliedT.setTextColor(0xFFFF0000);
            return;
        }

        if(frequency.contains("Select"))
        {

            TextView frequencyT= findViewById(R.id.frequencytext);
            frequencyT.setError("Please select one of the options below");
            frequencyT.requestFocus();
            frequencyT.setTextColor(0xFFFF0000);
            return;
        }

        if(targetarea.contains("Select"))
        {

            TextView targetT= findViewById(R.id.targetareatext);
            targetT.setError("Please select one of the options below");
            targetT.requestFocus();
            targetT.setTextColor(0xFFFF0000);
            return;
        }

        if(childgrades.contains("Select"))
        {
            TextView gradesT= findViewById(R.id.gradestext);
            gradesT.setError("Please select one of the options below");
            gradesT.requestFocus();
            gradesT.setTextColor(0xFFFF0000);
            return;
        }
        if(childmood.contains("Select"))
        {
            TextView moodT= findViewById(R.id.moodtext);
            moodT.setError("Please select one of the options below");
            moodT.requestFocus();
            moodT.setTextColor(0xFFFF0000);
            return;
        }

        if (digitalsecrecy.contains("Select"))
        {
            TextView digitalT= findViewById(R.id.digitalsecrecytext);
            digitalT.setError("Please select one of the options below");
            digitalT.requestFocus();
            digitalT.setTextColor(0xFFFF0000);
            return;
        }

        if (avoidance.contains("Select"))
        {
            TextView avoidanceT= findViewById(R.id.avoidancetext);
            avoidanceT.setError("Please select one of the options below");
            avoidanceT.requestFocus();
            avoidanceT.setTextColor(0xFFFF0000);
            return;
        }





        if (!Patterns.EMAIL_ADDRESS.matcher(ema).matches()) //checks for valid email
        {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return;
        }
        if (pass.isEmpty()){
            password.setError("Password is required");
            password.requestFocus();
            return;}

        if (pass.length()<6){
            password.setError("Please enter a password with a minimum length of 6");
            password.requestFocus();
            return;
        }

       PB.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(ema, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                   // CALCULATE THE STATIC SCORE AND STORE IT
                    if (sexuality.contentEquals("Bisexual")) suscscoreforuser += 0.5;
                    else if (sexuality.contentEquals("Homosexual")) suscscoreforuser += 0.8;

                    //increment score for age
                    if (age.contentEquals("8-10")) suscscoreforuser +=0.2;
                    else if (age.contentEquals("11-13")) suscscoreforuser+=0.4;
                    else if (age.contentEquals("14-18")) suscscoreforuser +=0.6;

                    //increment score for religion
                    if(religion.contentEquals("Minority")) suscscoreforuser +=0.4;

                    //increment for mental health history
                    if(mentalheathhistory.contentEquals("Anxiety")) suscscoreforuser +=0.5;
                    else if(mentalheathhistory.contentEquals("Depression")) suscscoreforuser +=0.7;

                    if(physicalstruggles.contentEquals("Obesity")) suscscoreforuser+=0.5;
                    else if(physicalstruggles.contentEquals("Anorexia")) suscscoreforuser +=0.5;


                    if(digitalsecrecy.contentEquals("Yes")) suscscoreforuser +=0.3;
                    if(childmood.contentEquals("Yes")) suscscoreforuser +=0.3;
                    if(avoidance.contentEquals("Yes")) suscscoreforuser +=0.3;
                    if(childgrades.contentEquals("Yes")) suscscoreforuser +=0.3;

                    suscscoreforuser = (float) (suscscoreforuser/4.20);
                 String susscore=Float.toString(suscscoreforuser);
                    User u = new User(
                            ema,
                            age,
                            religion,
                             gender,
                            mentalheathhistory,
                             country,
                             frequency,
                             targetarea,
                             childgrades,
                             childmood,
                             digitalsecrecy,
                             twitteru,
                             firstn,
                             lastn,
                             sexuality,
                             avoidance,
                             bulliedbefore,
                            physicalstruggles,
                            susscore

                    );
                    FirebaseDatabase data=FirebaseDatabase.getInstance();
                          DatabaseReference myref=data.getReference("Users");
                          myref.child(mAuth.getCurrentUser().getUid()).setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Log.d("hey","hey");
                                finish();
                                PB.setVisibility(View.INVISIBLE);
                                Intent w = new Intent(getApplicationContext(), MainActivity.class);
                                w.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //ensures when we back it doesnt go back to the login
                                startActivity(w);
                            }
                            else {
                               Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                            }                        }
                    });


                }
                else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException)
                    {
                        PB.setVisibility(View.GONE);
                            email.setError("This email is already registered");
                            email.requestFocus();
                    }
                    else Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    //////////MENU DECLARATION////////////

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.childregistration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {


            case R.id.nextButton: //if you press on the Home button, return to the main page (main activity)
                Intent w = new Intent(this, MainActivity.class);
                startActivity(w);
                return true;
            case R.id.backButton:
                Intent s = new Intent(this, MainActivity.class); //for now consider main to be home page
                startActivity(s);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonxm:
            registerUser();
            break;
        }
    }

    public boolean isAlpha(String name) { //checks if only letters
        return name.matches("[a-zA-Z]+");
    }

}