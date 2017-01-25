package com.vrstudios.v.photo2blog;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private ImageView mDisplayrofile;
    private TextView mProfileName;
    private TextView mEmailid;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;

    Context mcontext;
    View RootView;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RootView= inflater.inflate(R.layout.fragment_profile, container, false);

        mcontext = container.getContext();

        mDisplayrofile=(ImageView)RootView.findViewById(R.id.Displayprofile);
        mProfileName=(TextView)RootView.findViewById(R.id.ProfileNameTv);
        mEmailid=(TextView)RootView.findViewById(R.id.EmailidTv);

        mAuth=FirebaseAuth.getInstance();
        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mAuthListener=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser()!=null){

                }else{
                    Toast.makeText(getContext(),"current user null",Toast.LENGTH_SHORT).show();
                }

            }
        };

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=(String)dataSnapshot.child("name").getValue();
                String emailid=mAuth.getCurrentUser().getEmail().toString();
                String userprofileimage=(String)dataSnapshot.child("image").getValue();

                long countval=dataSnapshot.getChildrenCount();

                mProfileName.setText(name);
                mEmailid.setText(emailid);


                Glide.with(getActivity()).load(userprofileimage).into(mDisplayrofile);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return  RootView;

    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
    }
}
