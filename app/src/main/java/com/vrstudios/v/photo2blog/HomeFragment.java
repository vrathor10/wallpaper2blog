package com.vrstudios.v.photo2blog;


import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private  static final int PERMS_REQUEST_CODE = 123;

View rootView;
    private RecyclerView mBlogList;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;

    private boolean mProcessLike=false;

    private DatabaseReference mDatabaseLike;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_home, container, false);

        mAuth=FirebaseAuth.getInstance();

        mAuthListner=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if((firebaseAuth.getCurrentUser())==null){
                    Intent loginIntent= new Intent(getActivity(),LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }else{
                    checkUserExist();
                }

            }
        };

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mDatabase.keepSynced(true);

        mBlogList=(RecyclerView)rootView.findViewById(R.id.Blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(getContext()));

        ////////////////admob integration ////////////////////
        MobileAds.initialize(getActivity(), "ca-app-pub-2534568726881165/4233728533");
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mAdView.loadAd(adRequest);
        /////////////////////////////////////////////////////

/*////////////////////////////////////////////////*/
        final View decorView = ((Activity) getContext()).getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                             View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });
///////////////////////////////////////////////////
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

          /*hide soft keys */

        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = ((Activity)getContext()).getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(getContext(),"swipe LEFT or RIGHT ",Toast.LENGTH_LONG).show();
        mAuth.addAuthStateListener(mAuthListner);

        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class ,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, Blog model, int position) {

                final  String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getContext(),model.getImage());
                viewHolder.setUsername(model.getUsername());


                viewHolder.setmLikeBtn(post_key);
                viewHolder.setmLikeCount(post_key);

                viewHolder.mDownloadImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        
                        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String post_image = (String) dataSnapshot.child("image").getValue();
                                if (hasPermissions()){
                                    // our app has permissions.
                                    viewHolder.DownloadImage(getContext(),post_image,post_key);
                                }
                                else {
                                    //our app doesn't have permissions, So i m requesting permissions.
                                    requestPerms();
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getContext(), "failed to downoad...", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });

                viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mProcessLike=true;

                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (mProcessLike) {

                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                            mProcessLike = false;

                                        } else {

                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Random string text");

                                            mProcessLike = false;
                                        }

                                    }
                                }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    }

                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(getContext(),"image clicked",Toast.LENGTH_SHORT).show();

                        Intent singleBlogIntent=new Intent(getActivity(),BlogSingleActivity.class);
                        singleBlogIntent.putExtra("blog_id",post_key);
                        startActivity(singleBlogIntent);

                    }
                });
            }
        };



        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(getActivity(), SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
////////////////////////////////////////////////permission handled here////////////////////////////

        private boolean hasPermissions(){
            int res = 0;
            //string array of permissions,
            String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

            for (String perms : permissions){
                res = PermissionChecker.checkCallingOrSelfPermission(getContext(),perms);
                if (!(res == PackageManager.PERMISSION_GRANTED)){
                    return false;
                }
            }
            return true;
        }

    private void requestPerms(){
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions,PERMS_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode){
            case PERMS_REQUEST_CODE:

                for (int res : grantResults){
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed){
            //user granted all permissions we can perform our task.

        }
        else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(getContext(), "Storage Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }


 ///////////////////////////////////////////////////////////////////////////////////////////////
    }


    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton mLikeBtn;
        TextView mLikesCount;
        ImageButton mDownloadImageBtn;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);

            mView=itemView;

            mLikeBtn=(ImageButton)mView.findViewById(R.id.like_btn);
            mLikesCount=(TextView)mView.findViewById(R.id.LikesCount);
            mDownloadImageBtn=(ImageButton)mView.findViewById(R.id.DownloadImagebtn);

            mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth=FirebaseAuth.getInstance();

            mDatabaseLike.keepSynced(true);

        }



        public void setmLikeCount(final String mPostkey){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    mLikesCount.setText(dataSnapshot.child(mPostkey).getChildrenCount()+"");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setmLikeBtn(final String post_key) {
            if (mAuth.getCurrentUser() != null) {
                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                            mLikeBtn.setImageResource(R.drawable.like_btn_red);
                        } else {
                            mLikeBtn.setImageResource(R.drawable.like_btn_grey);

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        public void DownloadImage(final Context context, final String post_url,final String imgTitle){

            Picasso.with(context)
                    .load(post_url)
                    .into(new Target()
                    {

                        @Override
                        public void onPrepareLoad(Drawable drawable)
                        {
                        }

                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
                        {
                            managedownloadImage(imgTitle,bitmap);

                        }

                        @Override
                        public void onBitmapFailed(Drawable arg0)
                        {
                        }
                    });
        }

        public void managedownloadImage(String imgTitle,Bitmap bitmap){
            String folderPath=Environment.getExternalStorageDirectory() + "/Photo2Blog";
            File file = new File(folderPath);
            if (!file.exists()) {
                file.mkdir();

            }
            File filename = new File(folderPath+"/"+imgTitle+"_img.jpg");

            try {
                filename.createNewFile();
                FileOutputStream ostream = new FileOutputStream(filename);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                ostream.close();
                mDownloadImageBtn.setImageResource(R.mipmap.ic_done_black_24dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public  void  setTitle(String title){
            TextView post_title = (TextView)mView.findViewById(R.id.Post_title);
            post_title.setText(title);
        }
        public  void  setDesc(String desc){
            TextView post_desc = (TextView)mView.findViewById(R.id.Post_text);
            post_desc.setText(desc);
        }


        public void setImage(final Context ctx, final String image){
            final ImageView post_image=(ImageView)mView.findViewById(R.id.Post_image);

            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx).load(image).into(post_image);

                }
            });


        }
        public void setUsername(String username){
            TextView post_username = (TextView)mView.findViewById(R.id.post_user_name);
            post_username.setText("Posted_by : "+username);
        }
    }



}
