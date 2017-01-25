package com.vrstudios.v.photo2blog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemProfile;
    private ResideMenuItem itemSettings;
    private ResideMenuItem itemUpload;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;
        setUpMenu();

        if(savedInstanceState == null){
            changeFragment(new HomeFragment());
        }
    }

    private void setUpMenu() {
        resideMenu = new ResideMenu(this);

        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);


        resideMenu.setScaleValue(0.6f);

        itemHome=new ResideMenuItem(this,R.drawable.icon_home,"Home");
        itemProfile=new ResideMenuItem(this,R.drawable.icon_profile,"Profile");
        itemSettings=new ResideMenuItem(this,R.drawable.icon_settings,"Settings");
        itemUpload=new ResideMenuItem(this,R.drawable.icon_upload,"Upload");

        itemHome.setOnClickListener(this);
        itemUpload.setOnClickListener(this);
        itemProfile.setOnClickListener(this);
        itemSettings.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome,ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemProfile,ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings,ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(itemUpload,ResideMenu.DIRECTION_RIGHT);

        findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });

        findViewById(R.id.title_bar_right_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
            }
        });



    }
    private  ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener(){
        @Override
        public void openMenu() {

        }

        @Override
        public void closeMenu() {

        }
    };

    @Override
    public void onClick(View v) {
        if(v== itemHome){
            changeFragment(new HomeFragment());
        }
        else if(v== itemProfile){
            changeFragment(new ProfileFragment());
        }
        else if(v== itemUpload){
            changeFragment(new UploadFragment());
        }
        else if(v== itemSettings){
            changeFragment(new SettingsFragment());
        }
        resideMenu.closeMenu();
    }

    private void changeFragment(Fragment targetFragment){

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment,targetFragment,"fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }



    public ResideMenu getResideMenu(){
        return resideMenu;
    }
}
