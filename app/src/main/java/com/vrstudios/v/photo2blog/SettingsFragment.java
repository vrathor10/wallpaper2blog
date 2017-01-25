package com.vrstudios.v.photo2blog;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private TextView mAdmobFullscrnBtn;
    private TextView mChangePasswordSettingBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;

    View mSettingView;

    private Button mLogoutBtn;
    private TextView mEditAcc;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSettingView= inflater.inflate(R.layout.fragment_settings, container, false);

       mAuth=FirebaseAuth.getInstance();

       /* mAuthListner=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser()!=null){
                    //Toast.makeText(getContext(),"loged in as '.......'",Toast.LENGTH_LONG).show();

                }else{
                   /* Intent loginIntent= new Intent(getContext(),LoginActivity.class);
                    startActivity(loginIntent);
                    getActivity().finish();*//*
                }

            }
        };

/*/
        mLogoutBtn=(Button)mSettingView.findViewById(R.id.logoutbtn);
        mEditAcc=(TextView)mSettingView.findViewById(R.id.editprofileBtn);
        mAdmobFullscrnBtn=(TextView)mSettingView.findViewById(R.id.AdmobInterstitialBtn);
        mChangePasswordSettingBtn=(TextView)mSettingView.findViewById(R.id.changePasswordSettingBtn);

        mChangePasswordSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),ChangePasswordActivity.class));
            }
        });

        mAdmobFullscrnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),AdmobInterstitialActivity.class));
            }
        });

        mEditAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent AccountSetupIntent= new Intent(getContext(),SetupActivity.class);
                startActivity(AccountSetupIntent);

            }
        });

        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"cicked",Toast.LENGTH_LONG).show();
                LogOut();
            }
        });

        return mSettingView;
    }

    private void LogOut() {
        mAuth.signOut();
        Intent loginIntent= new Intent(getContext(),LoginActivity.class);
        startActivity(loginIntent);

    }

    @Override
    public void onStart() {
        super.onStart();
      //  mAuth.addAuthStateListener(mAuthListner);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAuthListner != null) {
          //  mAuth.removeAuthStateListener(mAuthListner);
        }
    }



}
