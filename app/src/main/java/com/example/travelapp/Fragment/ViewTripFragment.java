package com.example.travelapp.Fragment;

import android.content.Context;
import android.os.Bundle;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.travelapp.R;
import com.example.travelapp.configs.Constants;
import com.example.travelapp.models.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewTripFragment extends Fragment {

    ImageView mImage;
    TextView mTitle;
    TextView mCity;
    TextView mState;
    TextView mDate;
    TextView mDays;
    TextView mDescription;
    Button mEdit;
    DatabaseReference mDatabaseReference;

    private String mTripId;
    private Trip mTrip;

    private static final String TAG = "ViewTripFragment";
    public static final String ARGUMENT_TRIPID = "TripID";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTripId = (String) getArguments().get(ARGUMENT_TRIPID);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_TRIPS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);
        mImage = view.findViewById(R.id.trip_image);
        mTitle = view.findViewById(R.id.trip_title);
        mCity = view.findViewById(R.id.trip_city);
        mState = view.findViewById(R.id.trip_state);
        mDate = view.findViewById(R.id.trip_date);
        mDays = view.findViewById(R.id.trip_number_of_days);
        mDescription = view.findViewById(R.id.trip_description);
        mEdit = view.findViewById(R.id.edit_trip_button);
        getTripInfo();
        addButtonClickListener();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void getTripInfo() {
        Query query = mDatabaseReference.orderByKey().equalTo(mTripId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                    if (singleSnapshot != null) {
                        mTrip = singleSnapshot.getValue(Trip.class);
                        Picasso.get().load(mTrip.getImage()).into(mImage);
                        if (mTrip.getTitle().length() > 0) {
                            mTitle.setText(mTrip.getTitle());
                        } else {
                            mTitle.setVisibility(View.GONE);
                        }
                        mCity.setText(mTrip.getCity());
                        mState.setText(Constants.MAP_NAMES[mTrip.getState()]);
                        if (mTrip.getDate().length() > 0) {
                            mDate.setText(mTrip.getDate());
                        } else {
                            mDate.setVisibility(View.GONE);
                        }
                        mDays.setText(String.valueOf(mTrip.getDays()));
                        if (mTrip.getDescription().length() > 0) {
                            mDescription.setText(mTrip.getDescription());
                        } else {
                            mDescription.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addButtonClickListener() {
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString(ARGUMENT_TRIPID, mTripId);
                AddTripFragment fragment = new AddTripFragment();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment, "Edit_Trip");
                fragmentTransaction.addToBackStack("Edit_Trip");
                fragmentTransaction.commit();
            }
        });
    }
}
