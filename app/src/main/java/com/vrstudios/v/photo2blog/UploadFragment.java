package com.vrstudios.v.photo2blog;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends Fragment {

    private ProgressDialog mProgress;
    private Button mSubmitbtn;
    private ImageButton mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDesc;


    private  Uri mImageUri=null;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUser;

    View rootView;


    private static final int GALLERY_REQUEST=1;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;


    public UploadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         rootView=inflater.inflate(R.layout.fragment_upload, container, false);

        mProgress = new ProgressDialog(getContext());

        mAuth=FirebaseAuth.getInstance();

        mCurrentUser=mAuth.getCurrentUser();

        mDatabaseUser=FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mStorage= FirebaseStorage.getInstance().getReference();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");

        mPostTitle=(EditText)rootView.findViewById(R.id.titleField);
        mPostDesc=(EditText)rootView.findViewById(R.id.descField);

        mSubmitbtn=(Button)rootView.findViewById(R.id.UploadSubmitBtn);
        mSubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startPosting();
            }


        });


        mSelectImage=(ImageButton)rootView.findViewById(R.id.ProfileImageBtn);
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);

            }
        });
///////////////////////////////////
        final View mdecorView = ((Activity)getContext()).getWindow().getDecorView();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);
        mdecorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    mdecorView.setSystemUiVisibility(
                                     View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                             | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                             | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


                }
            }
        });
/////////////////////////////////////////////////////
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();

          /*hide soft keys */
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = ((Activity)getContext()).getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

    }
    private void startPosting() {
        mProgress.setMessage("Posting to Blog....");

        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDesc.getText().toString().trim();

        if( mImageUri !=null){

            if(!TextUtils.isEmpty(title_val) ) {

                if(!TextUtils.isEmpty(desc_val)) {

                    mProgress.show();

                    StorageReference filepath = mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());

                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            final DatabaseReference newPost = mDatabase.push();


                            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    newPost.child("title").setValue(title_val);
                                    newPost.child("desc").setValue(desc_val);
                                    newPost.child("image").setValue(downloadUrl.toString());
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                // change fragment from upload to home //
                                                Fragment newFragment = new HomeFragment();
                                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                transaction.replace(R.id.main_fragment, newFragment);
                                                transaction.addToBackStack(null);
                                                transaction.commit();

                                            } else {
                                                Toast.makeText(getContext(), "Fialed to uplaod", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mProgress.dismiss();
                        }
                    });
                }else {
                    mPostDesc.setError("Field cannot be left blank.");
                }
            }else{
                mPostTitle.setError("Field cannot be left blank.");
            }
        }else
        {
            Toast.makeText(getContext(),"select Image To Upload...",Toast.LENGTH_LONG).show();
        }
    }

    @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode== Activity.RESULT_OK){
          mImageUri=data.getData();
         //   mSelectImage.setImageURI(mImageUri);
            CropImage.activity(mImageUri)
                    .setAspectRatio(16,9)
                    .start(getContext(), this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {

                Uri resultUri = result.getUri();
                mSelectImage.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

}
