package com.haha.pluginapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.haha.pluginapp.databinding.PluginActivityPluginBinding;

public class PluginActivity extends BaseActivity {

    private static String TAG = PluginActivity.class.getSimpleName();

//    private AppBarConfiguration appBarConfiguration;
//    private PluginActivityPluginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Context = " + ((Context) this).getClass().getName());
        Log.e(TAG, "PluginActivity onCreate");
        Log.e(TAG, "Context = " + getBaseContext().getClass().getName());
        Log.e(TAG, "application = " + getApplication().getClass().getName());
        Log.e(TAG, "resources = " + getResources().getClass().getName());
        // 动态设置资源
        getResources().getIdentifier("plugin_activity_plugin", "layout", "com.haha.pluginapp");
        setContentView(R.layout.plugin_activity_plugin); // 使用合并后的资源ID
        getResources().getIdentifier("Base_Theme_HahaLearn", "values/themes", "com.haha.pluginapp");
        setTheme(R.style.Base_Theme_HahaLearn);
//        binding = PluginActivityPluginBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

//        setSupportActionBar(binding.toolbar);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_plugin);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//
//        binding.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAnchorView(R.id.fab)
//                .setAction("Action", null).show());
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.plugin_menu_plugin, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_plugin);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}