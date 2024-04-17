package com.sutd.t4app.ui.restaurant;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.sutd.t4app.R;
import com.sutd.t4app.data.model.Restaurant;
import com.sutd.t4app.data.model.Review;
import com.sutd.t4app.databinding.FragmentDashboardBinding;
import com.sutd.t4app.databinding.FragmentRestuarantProfileBinding;
import com.sutd.t4app.ui.home.HomeFragmentViewModel;
import com.sutd.t4app.ui.home.RestaurantExploreAdapter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RestaurantFragment extends Fragment implements OnMapReadyCallback{
    private FragmentRestuarantProfileBinding binding;
    private TextView textViewRestaurantLocation;
    private ImageView restImageHolder;
    private RatingBar Ratings;//Overall
    private TextView Menu1;
    private TextView Menu2;
    private TextView Menu3;
    private TextView Menu4;
    private RatingBar foodRating;
    private RatingBar serviceRating;
    private RatingBar atmosphereRating;
    private TextView User1;
    private TextView User1Review;
    private RatingBar User1Ratings;
    private TextView User2;
    private TextView User2Review;
    private RatingBar User2Ratings;
    private Restaurant restaurant;
    private ImageView restaurantProfileImage;
    private RestaurantFragmentViewModel viewModel;
    private ReviewListAdapter adapter;
    private TextView restaurantDescription;
    private MapView mapView;
    private GoogleMap googleMap;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentRestuarantProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Bundle arguments = getArguments();
        adapter = new ReviewListAdapter(new ArrayList<>(), R.layout.review_item );
        binding.reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewRecyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(RestaurantFragmentViewModel.class);


        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        String value = null;
        if (arguments != null) {
            restaurant = arguments.getParcelable("restaurant");
            TextView restaurantNameTextView = root.findViewById(R.id.textViewRestaurantName);
            //TextView restaurantNameTextView = root.findViewById(R.id.restaurantName);

            restaurantNameTextView.setText(restaurant.getName());
            Log.d("RestaurantData", "Restaurant name: " + restaurant.getName());
            Ratings=root.findViewById(R.id.ratingRest);
            Menu1= root.findViewById(R.id.Menu1);
            Menu2= root.findViewById(R.id.Menu2);
            Menu3= root.findViewById(R.id.Menu3);
            Menu4= root.findViewById(R.id.Menu4);
            foodRating=root.findViewById(R.id.foodRatingBar);
            serviceRating=root.findViewById(R.id.serivceRatingBar);
            atmosphereRating=root.findViewById(R.id.atmosphereRatingBar);
            Menu1.setText(restaurant.getTopMenu1());
            Log.d("RestaurantData", "Top Menu1: " + restaurant.getTopMenu1());
            Menu2.setText(restaurant.getTopMenu2());
            Menu3.setText(restaurant.getTopMenu3());
            Menu4.setText(restaurant.getTopMenu4());
            Ratings.setRating((float) restaurant.getRatings().doubleValue());
            foodRating.setRating((float) restaurant.getFoodRating().doubleValue());
            Log.d("RestaurantData", "foodrating: " + restaurant.getFoodRating());
            serviceRating.setRating((float) restaurant.getServiceRating().doubleValue());
            atmosphereRating.setRating((float) restaurant.getAmbienceRating().doubleValue());
            restaurantProfileImage = root.findViewById(R.id.restaurantProfileImage);
            restaurantDescription = root.findViewById(R.id.restDescription);
            restaurantDescription.setText(restaurant.getDescription());
            Picasso.get()
                    .load(restaurant.getImgMainURL()) // Assuming `getImageUrl()` is a method in your `Restaurant` class
                    .resize(350, 170)  // specify your desired size
                    .centerInside()
                    .into(restaurantProfileImage);
            viewModel.setcurrRes(restaurant);
            if (googleMap != null) {
                updateMapLocation(restaurant); // Update the map to show new location
            }
        } else {


        }


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getReviewsLiveData().observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null && !reviews.isEmpty()) {
                adapter.updateData(reviews);
                Log.d("LiveData Update", "Adapter updated with new data.");
            } else {
                Log.d("LiveData Update", "Received null or empty data.");
                // TODO: Handle empty or null data appropriately.
            }
        });

        Button btnCompare = view.findViewById(R.id.compareButton);
        btnCompare.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("restaurant", restaurant);
            Navigation.findNavController(v).navigate(R.id.compare_fragment, bundle);
        });
    }

    private void updateMapLocation(Restaurant restaurant) {
        if (restaurant != null) {
            LatLng location = new LatLng(Double.parseDouble(restaurant.getLat()), Double.parseDouble(restaurant.getLng()));
            googleMap.clear(); // Clear old markers
            googleMap.addMarker(new MarkerOptions().position(location).title(restaurant.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15)); // Adjust the zoom level as needed
            Log.d("MapDebug", "Map marker updated to: " + location.toString());
        } else {
            Log.e("MapError", "Restaurant data is null, cannot update map.");
        }
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        // This method is automatically called when the map is ready
        this.googleMap = googleMap; // Save a reference to the GoogleMap object
        setUpMap(); // Setup your map UI and functionality here

    }

    private void setUpMap() {
        if (restaurant != null) {
            double lat = Double.parseDouble(restaurant.getLat());
            double lng = Double.parseDouble(restaurant.getLng());
            LatLng location = new LatLng(lat, lng);

            Log.d("MapDebug", "Adding marker at location: " + location.toString());

            if (googleMap != null) {
                googleMap.addMarker(new MarkerOptions().position(location).title(restaurant.getName()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15)); // Adjust the zoom level as needed
            } else {
                Log.e("MapError", "GoogleMap object is null");
            }
        } else {
            Log.e("MapError", "Restaurant object is null");
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}