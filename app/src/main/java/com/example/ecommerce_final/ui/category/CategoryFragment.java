package com.example.ecommerce_final.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.adapters.CategoryAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.databinding.FragmentCategoryBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.CategoryProducts;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.login.RegisterFragment;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private CategoryViewModel categoryViewModel;
    private FragmentCategoryBinding binding;
    private CategoryDAO categoryDAO;
    private AppDatabase appDatabase;
    private CategoryAdapter categoryAdapter;
    private RecyclerView recyclerView;
    private User user;
    private PrefManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        retrieveSession();

        appDatabase = AppDatabase.getInstance(getContext());
        categoryDAO = appDatabase.categoryDAO();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        categoryAdapter = new CategoryAdapter(root.getContext(), user);

        CategoryViewModel categoryViewModel = new CategoryViewModel(appDatabase);
        recyclerView = binding.recyclerCategory;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setLayoutManager(new GridLayoutManager(root.getContext(), 2));
        recyclerView.setAdapter(categoryAdapter);

        categoryViewModel.getCategoryListLiveData().observe(this, new Observer<List<CategoryProducts>>() {
            @Override
            public void onChanged(List<CategoryProducts> categories) {
                categoryAdapter.setCategories(categories);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putInt("id", -1);
        binding.floatBtnAddCategory.setOnClickListener(v ->
                Navigation.findNavController(root)
                .navigate(R.id.category_to_registerCategory, bundle));

        if (!this.user.getRol().equals(User.ROL.SELLER)) {
            binding.floatBtnAddCategory.setVisibility(View.INVISIBLE);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void retrieveSession() {
        //create new session object by passing application context
        session = new PrefManager(getContext());
        //get User details if logged in
        user = session.getUserSession();
    }
}