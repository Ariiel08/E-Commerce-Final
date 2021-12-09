package com.example.ecommerce_final.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.ecommerce_final.models.CartProducts;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.CartServices;
import com.example.ecommerce_final.ui.login.LoginActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PrefManager {
    // Shared Preferences
    private final SharedPreferences sharedPreferences;
    // Editor for Shared preferences
    private final SharedPreferences.Editor editor;
    // Context
    private Context context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // SharedPreferences file name
    private static final String PREF_NAME = "SessionPref";
    private CartServices cartServices = CartServices.getInstance();
    private static final String USER = "user";
    private static final String IS_LOGIN = "isLoggedIn";
    private final Gson gson = new Gson();

    public PrefManager(Context context){
        this.context = context;

        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(final User user) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(USER, new Gson().toJson(user));
        // commit changes
        editor.commit();
    }

    public User getUserSession() {
        final String json = sharedPreferences.getString(USER, "{}");

        return new Gson().fromJson(json, User.class);
    }

    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent intent = new Intent(context, LoginActivity.class);
            // Closing all the Activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            context.startActivity(intent);
        }
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }

    public void logout() {
        editor.putBoolean(IS_LOGIN, false);
        editor.putString(USER, null);
        editor.commit();
    }

    public Object get(String key, Class c){
        if(c == Boolean.class){
            return sharedPreferences.getBoolean(key, false);
        }

        if(c == String.class){
            return sharedPreferences.getString(key, null);
        }

        return null;
    }

    public Set<String> getStringSet(String key){
        return sharedPreferences.getStringSet(key, null);
    }

    public void put(String key, Object value){
        if(value instanceof Boolean){
            editor.putBoolean(key, (Boolean) value);
        }

        if(value instanceof String){
            editor.putString(key, (String) value);
        }

        if(value instanceof Integer){
            editor.putInt(key, (int) value);
        }

        editor.commit();
    }

    public void putStringSet(String key, String value){
        Set<String> set = new HashSet<>();

        if(sharedPreferences.getStringSet(key, null) != null) {
            boolean valid = true;
            ArrayList<String> jsons = new ArrayList<>(sharedPreferences.getStringSet(key, null));
            ArrayList<CartProducts> cartProducts = new ArrayList<>();
            CartProducts cartProduct = gson.fromJson(value, CartProducts.class);

            for (String aux: jsons) {
                if(gson.fromJson(aux, CartProducts.class).getUid().equals(cartProduct.getUid())){
                    CartProducts cartProduct2 = gson.fromJson(aux, CartProducts.class);

                    cartServices.getCart().getProducts().removeIf(x -> x.getUid().equals(cartProduct2.getUid()));

                    cartProduct2.setQuantity(cartProduct2.getQuantity() + cartProduct.getQuantity());
                    cartServices.setCartProducts(cartProduct2);
                    valid = false;

                    editor.putStringSet(key, set);
                    editor.apply();

                    set = sharedPreferences.getStringSet(key, null);
                    System.out.println("size copy: "+cartServices.getCart().getProducts().size());
                    for (CartProducts x :cartServices.getCart().getProducts()) {
                        set.add(gson.toJson(x));
                    }
                }
            }

            if(valid){
                set.add(value);
                cartServices.setCartProducts(gson.fromJson(value, CartProducts.class));
            }
        }else{
            System.out.println("else");
            set.add(value);
            cartServices.setCartProducts(gson.fromJson(value, CartProducts.class));
        }

        for (String x: set) {
            System.out.println("el set de pref: "+x);
        }
        System.out.println("size: "+cartServices.getCart().getProducts().size());

        editor.putStringSet(key, set);
        editor.commit();

        System.out.println("size pref 2: "+sharedPreferences.getStringSet("products", null).size());
    }

    public String getString(String key){
        return sharedPreferences.getString(key, null);
    }

    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }

    public void removeAll(){
        editor.clear();
        editor.commit();
    }

    public SharedPreferences.Editor getEditor(){
        return editor;
    }

    public void update(String key, String value){
        Set<String> set = new HashSet<>();
        set.add(value);
        editor.putStringSet(key, set);
        editor.apply();
    }
}
