package com.vrstudios.v.photo2blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class BlogSingleActivity extends AppCompatActivity {

    String ImageUrl;
    private String mPost_key=null;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseLikes;

    private StorageReference mStroge;

    private FirebaseAuth mAuth;

    private ImageView mBlogSingleImage;
    private TextView mBlogSingleTitle;
    private TextView mBlogSingleDesc;
    private Button mSingleRemoveBtn;

    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        mAuth=FirebaseAuth.getInstance();

        mBlogSingleImage=(ImageView)findViewById(R.id.singleBlogImage);
        mBlogSingleTitle=(TextView)findViewById(R.id.singleBlogTitle);
        mBlogSingleDesc=(TextView)findViewById(R.id.singleBlogDesc);
        mSingleRemoveBtn=(Button)findViewById(R.id.singleRemoveBtn);


        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseLikes=FirebaseDatabase.getInstance().getReference().child("Likes");

        mPost_key=getIntent().getExtras().getString("blog_id");


        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title=(String)dataSnapshot.child("title").getValue();
                String post_desc=(String)dataSnapshot.child("desc").getValue();
                final String post_image=(String)dataSnapshot.child("image").getValue();
                String post_uid=(String)dataSnapshot.child("uid").getValue();
                ImageUrl=post_image;

                mBlogSingleTitle.setText(post_title);
                mBlogSingleDesc.setText(post_desc);

                Picasso.with(BlogSingleActivity.this).load(post_image).networkPolicy(NetworkPolicy.OFFLINE).into(mBlogSingleImage);

                mAttacher = new PhotoViewAttacher(mBlogSingleImage);
                mAttacher.update();


                if(mAuth.getCurrentUser().getUid().equals(post_uid)){
                    mSingleRemoveBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mSingleRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDatabase.child(mPost_key).removeValue();
                mDatabaseLikes.child(mPost_key).removeValue();

                mStroge=FirebaseStorage.getInstance().getReferenceFromUrl(ImageUrl);

                mStroge.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"deleted from storage",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       // Toast.makeText(getApplicationContext(),"failed to deleted from storage",Toast.LENGTH_SHORT).show();
                    }
                });
                Intent homeIntent=new Intent(BlogSingleActivity.this,MainActivity.class);
                startActivity(homeIntent);
                finish();

            }
        });

    }
}
