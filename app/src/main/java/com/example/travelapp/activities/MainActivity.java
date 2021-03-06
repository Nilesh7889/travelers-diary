package com.example.travelapp.activities;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Map;
import com.anychart.core.map.series.Choropleth;
import com.anychart.enums.HAlign;
import com.anychart.enums.SelectionMode;
import com.anychart.scales.LinearColor;
import com.example.travelapp.Fragment.AddTripFragment;
import com.example.travelapp.R;
import com.example.travelapp.configs.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddTripFragment.AddTripFragmentHandler {

    private RecyclerView recyclerView;
    private AnyChartView anyChartView;
    private FloatingActionButton mAddTripButton;


    public List<DataEntry> data;
    DatabaseReference mDatabaseReference;
    String mUid;
    long visitedStates;

    private static final String TAG = "Travelers-diary:MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        anyChartView = findViewById(R.id.any_chart_view);
        mAddTripButton = findViewById(R.id.add_trip_button);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

//        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_PATH_USERS);
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getStatesInfoAndConfigureMap();
        addClickListener();
    }

    private void getStatesInfoAndConfigureMap() {
        mDatabaseReference.child(mUid).child("statesInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long singleSnapshot = dataSnapshot.getValue(Long.class);
                    if (singleSnapshot != null) {
                        visitedStates = singleSnapshot;
                        Log.d(TAG, "Visited States: " + visitedStates);
                        configureMap();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private List<DataEntry> getData() {
        List<DataEntry> data = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            data.add(new CustomDataEntry(Constants.MAP_IDS[i],
                    Constants.MAP_NAMES[i],
                    getMapColor(i),
                    new LabelDataEntry(false)));
        }
        return data;
    }

    class CustomDataEntry extends DataEntry {
        public CustomDataEntry(String id, String name, Number value) {
            setValue("id", id);
            setValue("name", name);
            setValue("value", value);
        }
        public CustomDataEntry(String id, String name, Number value, LabelDataEntry label) {
            setValue("id", id);
            setValue("name", name);
            setValue("value", value);
            setValue("label", label);
        }
    }

    class LabelDataEntry extends DataEntry {
        LabelDataEntry(Boolean enabled) {
            setValue("enabled", enabled);
        }
    }

    private int getMapColor(int index) {
        // bit manipulation; get the digit at index index of visitedStates
        return (visitedStates & (1L << index)) == 0 ? 0 : 1;
    }

    private void configureMap() {

        Map map = AnyChart.map();


        map.padding(50,0,0,0);
        map.background("#2d2d2d");

        map.title()
                .enabled(true)
                .useHtml(true)
                .hAlign(String.valueOf(HAlign.CENTER))
                .fontFamily("Verdana, Helvetica, Arial, sans-serif")
                .padding(35, 0, 10, 0)
                .text("<span style=\"color:#00F3F6; font-size: 25 px\"> Travel Map" +
////                        "Love Wine The Most.</span> <br>" +
//                        "<br><span style=\"color:#545f69; font-size: 14px\"> United States of America" +
                        "</span>");
//
//        map.credits()
//                .enabled(true)
//                .url("https://en.wikipedia.org/wiki/List_of_sovereign_states_and_dependent_territories_by_population_density")
//                .text("Data source: https://en.wikipedia.org/wiki/List_of_sovereign_states_and_dependent_territories_by_population_density");
////                .logoSrc("https://static.anychart.com/images/maps_samples/USA_Map_with_Linear_Scale/favicon.ico");

        map.geoData("anychart.maps.united_states_of_america");



        map.interactivity().selectionMode(SelectionMode.MULTI_SELECT);
        map.padding(0, 0, 0, 0);


        Choropleth series = map.choropleth(getData());
        LinearColor linearColor = LinearColor.instantiate();
        linearColor.colors(new String[]{ "#e5e5e5", "#00F3F6"});
        series.colorScale(linearColor);
        series.hovered()
                .fill("#2E6171")
                .stroke("#2E6171");
        series.selected()
                .fill("#2E6171")
                .stroke("#2E6171");
        series.labels(true).enabled(true);
        series.labels().fontSize(10);
        series.labels().fontColor("ffffff");
        series.labels().format("{%Value}");

        series.tooltip()
                .useHtml(true)
                .format("function() {\n" +
                        "            return '<span style=\"font-size: 13px\"> </span>';\n" +
                        "          }");

        map.credits("N");
        map.credits().enabled(false);
        anyChartView.setLicenceKey("nilesh78890@gmail.com-885f8f7b-ef1cea3b");

        anyChartView.addScript("file:///android_asset/united_states_of_america.js");
        anyChartView.addScript("file:///android_asset/proj4.js");
        anyChartView.setChart(map);

    }

    private void addClickListener() {
        mAddTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().add(R.id.fragment_container, new AddTripFragment()).addToBackStack("Add a trip").commit();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // When there is a fragment attached, pop it and stay at the current activity, otherwise go to previous activity
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = menuItem -> {

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_search:
                Intent intent2 = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_travel_history:
                Intent intent3 = new Intent(MainActivity.this, TravelHistoryActivity.class);
                startActivity(intent3);
                break;
            case R.id.nav_notification:
                Intent intent4 = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent4);
                break;
            case R.id.nav_profile:
                Intent intent5 = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent5);
                break;

        }
        return false;
    };

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // get which view was focused
            View v = getCurrentFocus();

            if (HideKeyboard.getInstance().isTouchOutsideView(v, ev)) {
                HideKeyboard.getInstance().hideSoftInput(v.getWindowToken(), this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }


}

