package com.example.tabview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;


public class ProfileFragment extends Fragment implements View.OnClickListener {
private ImageView profileimage;
    private Uri uriimage;
    private static final int CHOOSE_IMAGE = 101;
    private Button logout;
    String  profileImageUrl;
    private FirebaseAuth Fauth=FirebaseAuth.getInstance();
    private FirebaseUser f= FirebaseAuth.getInstance().getCurrentUser();
    private Button save;
    private ProgressBar prg;
    private TextView email, fullname, agegroup, sexuality, twitterUsername, religion;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_profile, container, false);

        //TextView setups/////
        email=v.findViewById(R.id.emailProfileX);
        fullname=v.findViewById(R.id.nameProfileX);
        agegroup=v.findViewById(R.id.ageProfileX);
        sexuality=v.findViewById(R.id.sexualityProfileX);
        twitterUsername=v.findViewById(R.id.twitterProfileX);
        religion=v.findViewById(R.id.religionProfileX);
        ///////


        profileimage=v.findViewById(R.id.profilepicture);
        prg= (ProgressBar)v.findViewById(R.id.progressBar);
 LoadUserInformation();
        logout=v.findViewById(R.id.logoutbutton);
        save=v.findViewById(R.id.SaveImage22);
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(view.getId()==R.id.profilepicture)
                showImageChooser();

            }

        });
        logout.setOnClickListener(this);
        save.setOnClickListener(this);
        return v;


    }




    private void LoadUserInformation(){
Log.d("loading", "hi");

        //GET INFO OTHER THAN PROFILE PICTURE:

        FirebaseDatabase data=FirebaseDatabase.getInstance();
        DatabaseReference myref=data.getReference("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                User value = dataSnapshot.getValue(User.class); //stores the values in this user
                fullname.setText( value.getFname() + " " + value.getLname());
                email.setText(value.getEma());
                agegroup.setText(value.getAge());
                sexuality.setText(value.getSexuality());
                twitterUsername.setText(value.getTwitteru());
                religion.setText(value.getReligion());
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        ///////////////////






        if(f.getPhotoUrl()!=null) {
            Log.d("loading", "hi2");
         //   String photourl = currentuser.getPhotoUrl().toString();
            Log.d("hi",f.getPhotoUrl().toString());
            Log.d("hi",f.getDisplayName().toString());
         //   Glide.with(getView()).load(currentuser.getPhotoUrl().toString()).into(profileimage);
            Glide.with(this).load(f.getPhotoUrl()).into(profileimage);

        }

}
    private void saveUserInformation() {

        Log.e("EROOR", "h");
        if (f!=null && profileImageUrl!=null) {
            Log.e("EROOR", "h");
             UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(f.getEmail()).setPhotoUri(Uri.parse(profileImageUrl)).build();
        f.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
//                String x= Fauth.getCurrentUser().getPhotoUrl().toString();
//                    Log.e("EROORROROROROROR", x);
                    Log.d("hi", "he");
                }


            }
        }) .addOnFailureListener(new OnFailureListener() {
                                                                           @Override
                                                                           public void onFailure(@NonNull Exception e) {
                                                                               Log.d("hi", e+"");
                                                                           }
                                                                       }
        );
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CHOOSE_IMAGE &&resultCode== RESULT_OK && data!=null && data.getData()!=null)
        {


           uriimage= data.getData();
                decodeUri(uriimage);
            uploadImageToFirebaseStorage();

        }
    }

    private void showImageChooser()
    {
        Intent i= new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select profile image"), CHOOSE_IMAGE);



    }



    public void decodeUri(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        try {
            parcelFD = getActivity().getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor imageSource = parcelFD.getFileDescriptor();

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(imageSource, null, o);

            // the new size we want to scale to
            final int REQUIRED_SIZE = 1024;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

            profileimage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            // handle errors
        } catch (IOException e) {
            // handle errors
        } finally {
            if (parcelFD != null)
                try {
                    ((ParcelFileDescriptor) parcelFD).close();
                } catch (IOException e) {
                    // ignored
                }
        }
}

    private void uploadImageToFirebaseStorage() {

        prg.setVisibility(View.VISIBLE);
        final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");

        if (uriimage != null) {

            profileImageRef.putFile(uriimage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // profileImageUrl taskSnapshot.getDownloadUrl().toString(); //this is depreciated

                            //this is the new way to do it
                            profileImageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                     profileImageUrl=task.getResult().toString();
                                    Log.i("URL",profileImageUrl);
                                    prg.setVisibility(View.INVISIBLE);
                                    save.setEnabled(true);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            progressBar.setVisibility(View.GONE);
//                            Toast.makeText(ProfileActivity.this, "aaa "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onClick(View view) {

            switch (view.getId())
            {
                case R.id.SaveImage22:
                    saveUserInformation();
                    break;
                case R.id.logoutbutton:
                    FirebaseAuth.getInstance().signOut();
                    getActivity().finish();
                    startActivity(new Intent (getContext(), LoginPage.class));
                    break;


            }
    }
}