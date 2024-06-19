package com.store.faskcamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import com.dvinfosys.model.HeaderModel;
import com.dvinfosys.ui.NavigationListView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.Manifest.permission_group.CAMERA;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationListView listView;
    private EditText ipAddressEditText;
    private Button connectButton;
    private TextView connectionStatusTextView;
    private BroadcastReceiver mReceiver;
    private SendMessage pidgeon = new SendMessage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_layout);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbarTitle2);
        toolbarTitle.setText("Jetracer");
        setSupportActionBar(toolbar);

        ipAddressEditText = findViewById(R.id.ip_address_edit_text);
        connectButton = findViewById(R.id.connect_button);
        connectionStatusTextView = findViewById(R.id.connection_status_text_view);

        SharedPreferences sharedPref = getSharedPreferences("userConf", Context.MODE_PRIVATE);
        String savedIpAddress = sharedPref.getString("serverIp", "");

        if (!savedIpAddress.isEmpty()) {
            connectionStatusTextView.setText("Connected to " + savedIpAddress);
            connectionStatusTextView.setTextColor(Color.GREEN);
        }

        connectButton.setOnClickListener(v -> {
            String ipAddress = ipAddressEditText.getText().toString();
            if (!ipAddress.isEmpty()) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("serverIp", ipAddress);
                editor.apply();

                connectionStatusTextView.setText("Connected to " + ipAddress);
                connectionStatusTextView.setTextColor(Color.GREEN);
            } else {
                connectionStatusTextView.setText("IP Address cannot be empty");
                connectionStatusTextView.setTextColor(Color.RED);
            }
        });

        listView = findViewById(R.id.expandable_navigation);
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView.init(this)
                .addHeaderModel(new HeaderModel("Home"))
                .addHeaderModel(new HeaderModel("Jetracer"))
                .addHeaderModel(new HeaderModel("Jetracer control"))
                .addHeaderModel(new HeaderModel("Generate QR Code"))
                .build()
                .addOnGroupClickListener((parent, v, groupPosition, id) -> {
                    listView.setSelected(groupPosition);
                    if (id == 0) {
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    } else if (id == 1) {
                        startActivity(new Intent(MainActivity.this, LiveCameraJetracer.class));
                    } else if (id == 2) {
                        startActivity(new Intent(MainActivity.this, JetracerControlActivity.class));
                    } else if (id == 3) {
                        startActivity(new Intent(MainActivity.this, GenerateQR.class));
                    }
                    return false;
                });

        if (ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);
        }

        getFCMToken();

        IntentFilter intentFilter = new IntentFilter("com.store.faskcamera");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MainActivity.this, "Warning: Detection! Switch to video stream", Toast.LENGTH_LONG).show();
            }
        };
        this.registerReceiver(mReceiver, intentFilter);
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                pidgeon.execute("token=" + token);
                Log.i("My token", token);
            } else {
                Log.i("Task failed", "failed");
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
