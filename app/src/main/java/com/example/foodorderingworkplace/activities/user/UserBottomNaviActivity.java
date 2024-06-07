package com.example.foodorderingworkplace.activities.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.os.Bundle;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.foodorderingworkplace.R;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class UserBottomNaviActivity extends AppCompatActivity {
    //ids fragments
    private MeowBottomNavigation bottomNavigation;
    public FragmentContainerView containerView;
    private final static int Home=1;
    private final static int Order=2;
    private final static int Profile=3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bottomnavi);
        bottomNavigation = findViewById(R.id.bottom_navi);
        containerView = findViewById(R.id.container);

        bottomNavigation.add(new MeowBottomNavigation.Model(Home, R.drawable.baseline_home_white));
        bottomNavigation.add(new MeowBottomNavigation.Model(Order, R.drawable.baseline_shopping_cart_white));
        bottomNavigation.add(new MeowBottomNavigation.Model(Profile, R.drawable.baseline_person_white));

        //set current selectedFragment
        bottomNavigation.show(Home, true);
        //if button click show fragment
        bottomNavigation.setOnShowListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                //Initialize Fragments according to id
                Fragment fragment = null;
                if (model.getId()==1){
                    fragment = new HomeUserFragment();

                } else if (model.getId()==2) {
                    fragment = new ShopUserFragment();

                } else if (model.getId()==3) {
                    fragment = new ProfileEditUserFragment();
                }
                //method for lead and replace fragments
                loadAndReplaceFragment(fragment);
                return null;
            }
        });
    }

    private void loadAndReplaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, null).commit();
    }
}