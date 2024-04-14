package com.sutd.t4app.ui.home;
/*
 * The HomeFragment class in an Android app displays a list of restaurants and allows users to switch
 * between explore and feed pages.
 */
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sutd.t4app.R;
import com.sutd.t4app.data.model.Restaurant;
import com.sutd.t4app.databinding.FragmentHomeBinding;
import com.sutd.t4app.ui.ProfileQuestions.UserProfile;
import com.sutd.t4app.ui.ProfileQuestions.UserProfileViewModel;
import com.sutd.t4app.ui.filter.FilterViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private Button searchButton;
    private SearchView searchbar;
    private SwitchCompat pageSwitch;
    private ImageView filterIcon;
    private boolean isExplorePage = true; // initial is explore page
    private HomeFragmentViewModel viewModel;
    private RestaurantExploreAdapter adapter;
//    private RestaurantExploreAdapter hotAdapter;
    // TODO: 14/4/24 hotAdapter TikTokAdapter
    private TikTokAdapter hotAdapter;

    private FragmentHomeBinding binding;

    private ImageView questionnaire;
    private FilterViewModel filterViewModel;
    private boolean fromFilter= false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Existing binding setup
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Log.i("test","running FRAGMENT ACTIVITY");

        // Initialize the RecyclerView and its adapter
        adapter = new RestaurantExploreAdapter(new ArrayList<>(), R.layout.restaurant_item );
        binding.recyclerViewRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRestaurants.setAdapter(adapter);

//        hotAdapter = new RestaurantExploreAdapter(new ArrayList<>(), R.layout.restaurant_hot_item);
//        binding.recyclerViewHot.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
//        binding.recyclerViewHot.setAdapter(hotAdapter);
        hotAdapter = new TikTokAdapter(new ArrayList<>(), R.layout.tiktok_item_layout);
        binding.recyclerViewHot.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        binding.recyclerViewHot.setAdapter(hotAdapter);


        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);
        filterViewModel= new ViewModelProvider(requireActivity()).get(FilterViewModel.class);



        // Observe the LiveData from the ViewModel
//        viewModel.getRestaurantsLiveData().observe(getViewLifecycleOwner(), restaurants -> {
//            Log.d("HomeFragmenthotAdaptor", "Number of unranked restaurants received: " + restaurants.size());
//            // Use this data for something that doesn't need ranked data, for example, for `hotAdapter`
//            if (restaurants.size() >= 1) {
//
//                hotAdapter.updateData(restaurants);
//            }
//        });
        // TODO: 14/4/24 viewModel.getTikTokLiveData() & hotAdapter
        viewModel.getTikTokLiveData().observe(getViewLifecycleOwner(), tikToks -> {
            if (tikToks.size() >= 1) {
                Log.d("TikTokFetch", "TikToks received: " + tikToks.size());

                hotAdapter.updateDataTikTok(tikToks);
            }
        });


        // Observe the LiveData for ranked restaurants
        viewModel.getRankedRestaurantsLiveData().observe(getViewLifecycleOwner(), rankedRestaurants -> {

            if (rankedRestaurants.size() >= 1) {
                List<Restaurant> hotRestaurants = rankedRestaurants.subList(0, Math.min(2, rankedRestaurants.size()));
            // Update the adapter with the ranked list of restaurants
                 Log.d("HomeFragment", "Number of ranked restaurants received: " + rankedRestaurants.size());
                 adapter.updateData(rankedRestaurants);}
        });

        searchbar=root.findViewById(R.id.mySearchView);
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Filter restaurants when the user submits the query
                filterAndDisplayRestaurants(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter restaurants as the user types the query
                filterAndDisplayRestaurants(newText);
                return false;
            }
        });




        filterIcon = root.findViewById(R.id.filterIcon);
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fromFilter=false;
                Navigation.findNavController(v).navigate(R.id.tofilterfragment);
            }
        });

        pageSwitch = root.findViewById(R.id.pageSwitch);
        pageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showFeedPage();
                } else {
                    showExplorePage();
                }
            }
        });

        // Initially show the explore page
        showExplorePage();

        return root;
    }

    private void fetchData(){
//        viewModel.fetchRestaurantandUser();
        filterViewModel.getFilteredRestaurantsLiveData().observe(getViewLifecycleOwner(), filteredRestaurants -> {
            if (filteredRestaurants != null && !filteredRestaurants.isEmpty()) {
                Log.d("HomeFragmentFilterWidget", "Filtered restaurants received: " + filteredRestaurants.size());
                adapter.updateData(filteredRestaurants);
            } else {
                viewModel.fetchRestaurantandUser();
            }
        });
    }
    private void observeLiveData() {
        // Observe the filtered restaurant changes
        filterViewModel.getFilteredRestaurantsLiveData().observe(getViewLifecycleOwner(), filteredRestaurants -> {
            if (filteredRestaurants != null && !filteredRestaurants.isEmpty()) {
                Log.d("HomeFragment", "Filtered restaurants received: " + filteredRestaurants.size());
                adapter.updateData(filteredRestaurants);
            }
        });

        // Observe the full restaurant list only when no filtered data is available
        viewModel.getRestaurantsLiveData().observe(getViewLifecycleOwner(), restaurants -> {
            if ((filterViewModel.getFilteredRestaurantsLiveData().getValue() == null ||
                    filterViewModel.getFilteredRestaurantsLiveData().getValue().isEmpty()) && !restaurants.isEmpty()) {
                Log.d("HomeFragment", "Unfiltered restaurants received: " + restaurants.size());
                adapter.updateData(restaurants);
            }
        });

        // Observe the user profile for ranking updates
        viewModel.getUserProfilesLiveData().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                Log.d("HomeFragment", "User profile received for ranking.");
                viewModel.rankAndUpdateRestaurants(userProfile);
            }
        });

        // Observe the ranked restaurants
        viewModel.getRankedRestaurantsLiveData().observe(getViewLifecycleOwner(), rankedRestaurants -> {
            if (rankedRestaurants != null && !rankedRestaurants.isEmpty()) {
                adapter.updateData(rankedRestaurants);
            } else {
                Log.d("HomeFragment", "No ranked restaurants to display.");
            }
        });
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeLiveData();

//        viewModel.getRestaurantsLiveData().observe(getViewLifecycleOwner(), restaurants -> {
//            if (!restaurants.isEmpty()) {
//                triggerRanking();
//            }
//        });
//
//        viewModel.getUserProfilesLiveData().observe(getViewLifecycleOwner(), userProfile -> {
//            if (userProfile != null) {
//                // Use the user profile to rank restaurants, for example
//                viewModel.rankAndUpdateRestaurants(userProfile);
//            }
//        });
//
//        // Observe LiveData for ranked restaurants
//        viewModel.getRankedRestaurantsLiveData().observe(getViewLifecycleOwner(), rankedRestaurants -> {
//            // Update the adapter with the ranked list of restaurants
//            Log.d("HomeFragment", "Ranked restaurants updated.");
//            adapter.updateData(rankedRestaurants);
//        });
//
////        filterViewModel.getFilteredRestaurantsLiveData().observe(getViewLifecycleOwner(),filteredRestaurants ->{
////            // Update the adapter with the filtered list of restaurants
////            if (filteredRestaurants != null) {
////                Log.d("HomeFragmentFilterWidget", "Filtered restaurants received: " + filteredRestaurants.size());
////                adapter.updateData(filteredRestaurants);
////            }
////
////        });
//        filterViewModel.getFilteredRestaurantsLiveData().observe(getViewLifecycleOwner(), filteredRestaurants -> {
//            if (filteredRestaurants != null) {
//                Log.d("HomeFragmentFilterWidget", "Filtered restaurants received: " + filteredRestaurants.size());
//                adapter.updateData(filteredRestaurants);
//                fromFilter = true;  // Set the flag when data is received from filter
//            }
//        });
//        if (!fromFilter) {
//            viewModel.fetchRestaurantandUser();
//        }

    }
    private void showFeedPage() {
        isExplorePage = false;

        // Hide Explore layout and show Feed layout
        binding.exploreLayout.setVisibility(View.GONE);
        binding.feedLayout.setVisibility(View.VISIBLE);

    }

    private void showExplorePage() {
        isExplorePage = true;

        binding.feedLayout.setVisibility(View.GONE);
        binding.exploreLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void filterAndDisplayRestaurants(String query) {
        // Get the list of restaurants from the ViewModel
        List<Restaurant> allRestaurants = viewModel.getRestaurantsLiveData().getValue();

        if (allRestaurants != null) {
            // Perform filtering based on the search query
            List<Restaurant> filteredList = SearchRestaurants(allRestaurants, query);

            // Update the RecyclerView adapter with the filtered list of restaurants
            adapter.updateData(filteredList);
        }
    }
    private List<Restaurant> SearchRestaurants(List<Restaurant> restaurants, String query) {
        // Implement your filtering logic here
        List<Restaurant> filteredList = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            // Add the restaurant to the filtered list if it matches the query
            if (restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(restaurant);
            }
        }
        return filteredList;
    }
        private void triggerRanking() {
            if (!viewModel.getRestaurantsLiveData().getValue().isEmpty() && viewModel.getUserProfilesLiveData().getValue() != null) {
                viewModel.rankAndUpdateRestaurants(viewModel.getUserProfilesLiveData().getValue());
            }
        }
    }

