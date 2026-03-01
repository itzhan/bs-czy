package com.campus.zhihu.ui.home;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.campus.zhihu.R;
import com.campus.zhihu.ui.notification.NotificationFragment;
import com.campus.zhihu.ui.profile.ProfileFragment;
import com.campus.zhihu.ui.question.AskQuestionActivity;
import com.campus.zhihu.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private final HomeFragment homeFragment = new HomeFragment();
    private final SearchFragment searchFragment = new SearchFragment();
    private final NotificationFragment notificationFragment = new NotificationFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        FloatingActionButton fab = findViewById(R.id.fab_ask);

        activeFragment = homeFragment;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragment_container, notificationFragment, "notification").hide(notificationFragment)
                .add(R.id.fragment_container, searchFragment, "search").hide(searchFragment)
                .add(R.id.fragment_container, homeFragment, "home")
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target;
            int id = item.getItemId();
            if (id == R.id.nav_home) target = homeFragment;
            else if (id == R.id.nav_search) target = searchFragment;
            else if (id == R.id.nav_notification) target = notificationFragment;
            else if (id == R.id.nav_profile) target = profileFragment;
            else return false;
            getSupportFragmentManager().beginTransaction().hide(activeFragment).show(target).commit();
            activeFragment = target;
            return true;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, AskQuestionActivity.class)));
    }
}
