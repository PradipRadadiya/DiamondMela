package com.dealermela.cart.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dealermela.DealerMelaBaseFragment;
import com.dealermela.R;
import com.dealermela.authentication.myaccount.activity.LoginAct;
import com.dealermela.cart.adapter.CartAdapter;
import com.dealermela.cart.adapter.CartServerAdapter;
import com.dealermela.cart.model.CartLocalDataItem;
import com.dealermela.cart.model.CartServerDataItem;
import com.dealermela.dbhelper.DatabaseCartAdapter;
import com.dealermela.retrofit.APIClient;
import com.dealermela.retrofit.ApiInterface;
import com.dealermela.util.AppConstants;
import com.dealermela.util.AppLogger;
import com.dealermela.util.CommonUtils;
import com.dealermela.util.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.dealermela.home.activity.MainActivity.customerId;
import static com.dealermela.other.activity.SplashAct.loginFlag;

public class ShoppingFrg extends DealerMelaBaseFragment implements View.OnClickListener {

    private View rootView;
    private RecyclerView recycleViewCart;
    private TextView tvSubTotal, tvTax, tvGrandTotal;
    private float subTotal = 0, tax = 0, grandTotal = 0;
    private Button btnContinue;
    private DatabaseCartAdapter databaseCartAdapter;
    private Cursor c;

    public ShoppingFrg() {
        // Required empty public constructor
    }

    private SharedPreferences sharedPreferences;
    private ProgressBar progressBarCart;
    private LinearLayout linNoData;

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frg_shopping, parent, false);
        return rootView;
    }

    @Override
    public void init() {
        databaseCartAdapter = new DatabaseCartAdapter(getActivity());
        sharedPreferences = new SharedPreferences(Objects.requireNonNull(getActivity()));
    }

    @Override
    public void initView() {
        btnContinue = rootView.findViewById(R.id.btnContinue);
        recycleViewCart = rootView.findViewById(R.id.recycleViewCart);
        tvSubTotal = rootView.findViewById(R.id.tvSubTotal);
        tvTax = rootView.findViewById(R.id.tvTax);
        tvGrandTotal = rootView.findViewById(R.id.tvGrandTotal);
        progressBarCart = rootView.findViewById(R.id.progressBarCart);
        linNoData = rootView.findViewById(R.id.linNoData);

    }

    @Override
    public void postInitView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recycleViewCart.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void addListener() {
        btnContinue.setOnClickListener(this);
    }

    @Override
    public void loadData() {
        if (sharedPreferences.getLoginData().equalsIgnoreCase("")) {
            fillListView();
        } else {
            listCart(customerId);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnContinue:
                if (sharedPreferences.getLoginData().equalsIgnoreCase("")) {
                    Snackbar snackBar = Snackbar
                            .make(v, "Please first login", Snackbar.LENGTH_LONG)
                            .setAction("SIGN IN", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    loginFlag = 1;
                                    Intent intent = new Intent(getActivity(), LoginAct.class);
                                    startActivity(intent);
                                    Objects.requireNonNull(getActivity()).finish();
                                }
                            });
                    snackBar.setActionTextColor(Color.RED);
                    View snackBarView = snackBar.getView();
//                    snackBarView.setBackgroundColor(Color.DKGRAY);
                    TextView textView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snackBar.show();

                } else {
                    AppLogger.e("click", "----------");
                    Fragment fragment = new ShippingFrg();
                    FragmentManager fm = getFragmentManager();
                    assert fm != null;
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frameCart, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
                break;
        }
    }

    //View all data in local database
    private void fillListView() {
        List<CartLocalDataItem> cartLocalDataItems = new ArrayList<>();
        databaseCartAdapter.openDatabase();
        c = databaseCartAdapter.getAllValues();

        if (c.getCount()>0) {
            btnContinue.setEnabled(true);
            for (int i = 0; i < c.getCount(); i++) {
                AppLogger.e("ID", "-----------" + c.getInt(c.getColumnIndex(DatabaseCartAdapter.ID)));
                AppLogger.e("PRODUCT_ID", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.PRODUCT_ID)));
                AppLogger.e("PRODUCT_TYPE", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.PRODUCT_TYPE)));
                AppLogger.e("CATEGORY_ID", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.CATEGORY_ID)));
                AppLogger.e("SKU", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.SKU)));
                AppLogger.e("RING_SIZE", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.RING_SIZE)));
                AppLogger.e("BANGLE_SIZE", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.BANGLE_SIZE)));
                AppLogger.e("BRACELET_SIZE", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.BRACELET_SIZE)));
                AppLogger.e("PENDENT_SET_TYPE", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.PENDENT_SET_TYPE)));
                AppLogger.e("METAL_DETAIL", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.METAL_DETAIL)));
                AppLogger.e("STONE_DETAIL", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.STONE_DETAIL)));
                AppLogger.e("PRICE", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.PRICE)));
                AppLogger.e("QTY", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.QTY)));
                AppLogger.e("image url", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.PRODUCT_IMAGE)));
                AppLogger.e("RING_OPTION_ID", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.RING_OPTION_ID)));
                AppLogger.e("RING_OPTION_TYPE_ID", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.RING_OPTION_TYPE_ID)));
                AppLogger.e("STONE_OPTION_ID", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.STONE_OPTION_ID)));
                AppLogger.e("STONE_OPTION_TYPE_ID", "-----------" + c.getString(c.getColumnIndex(DatabaseCartAdapter.STONE_OPTION_TYPE_ID)));

                float price = Float.parseFloat(c.getString(c.getColumnIndex(DatabaseCartAdapter.PRICE))) * Float.parseFloat(c.getString(c.getColumnIndex(DatabaseCartAdapter.QTY)));
                cartLocalDataItems.add(new CartLocalDataItem(c.getInt(c.getColumnIndex(DatabaseCartAdapter.ID)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.PRODUCT_ID)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.PRODUCT_TYPE)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.CATEGORY_ID)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.SKU)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.RING_SIZE)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.BANGLE_SIZE)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.BRACELET_SIZE)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.PENDENT_SET_TYPE)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.METAL_DETAIL)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.STONE_DETAIL)),
                        String.valueOf(price),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.QTY)),
                        c.getString(c.getColumnIndex(DatabaseCartAdapter.PRODUCT_IMAGE))));

                AppLogger.e("price", "---------" + price);

                subTotal = subTotal + price;

                c.moveToNext();

                if (i == c.getCount() - 1) {

                    CartAdapter cartAdapter = new CartAdapter(getActivity(), cartLocalDataItems, ShoppingFrg.this);
                    recycleViewCart.setAdapter(cartAdapter);

                    AppLogger.e("sub Total", "---------" + subTotal);
                    tax = (subTotal * 3) / 100;
                    AppLogger.e("tax", "---------" + tax);

                    grandTotal = subTotal + tax;
                    AppLogger.e("grand total", "---------" + grandTotal);

                    tvSubTotal.setText(CommonUtils.priceFormat(subTotal));
                    tvTax.setText(String.valueOf(CommonUtils.priceFormat(tax)));
                    tvGrandTotal.setText(String.valueOf(CommonUtils.priceFormat(grandTotal)));


                }
            }
            databaseCartAdapter.closeDatabase();
        }else{
            linNoData.setVisibility(View.VISIBLE);
            btnContinue.setVisibility(View.INVISIBLE);
        }

    }

    public void updateCartItem(String id, String qty) {
        showProgressDialog("Quantity", getString(R.string.please_wait));
        databaseCartAdapter.openDatabase();
        databaseCartAdapter.updateQTY(id, qty);
        databaseCartAdapter.closeDatabase();
        hideProgressDialog();
        updateGrandTotal();
    }

    public void deleteItem(String id) {
        showProgressDialog("Quantity", getString(R.string.please_wait));
        databaseCartAdapter.openDatabase();
        databaseCartAdapter.deleteItem(id);
        databaseCartAdapter.closeDatabase();
        hideProgressDialog();
        updateGrandTotal();
    }

    public void updateGrandTotal() {
        subTotal = 0;
        tax = 0;
        grandTotal = 0;
        databaseCartAdapter.openDatabase();
        c = databaseCartAdapter.getAllValues();
        for (int i = 0; i < c.getCount(); i++) {

            float price = Float.parseFloat(c.getString(c.getColumnIndex(DatabaseCartAdapter.PRICE))) * Float.parseFloat(c.getString(c.getColumnIndex(DatabaseCartAdapter.QTY)));

            AppLogger.e("price", "---------" + price);
            subTotal = subTotal + price;

            c.moveToNext();
            if (i == c.getCount() - 1) {
                AppLogger.e("sub Total", "---------" + subTotal);
                tax = (subTotal * 3) / 100;
                AppLogger.e("tax", "---------" + tax);

                grandTotal = subTotal + tax;
                AppLogger.e("grand total", "---------" + grandTotal);

                tvSubTotal.setText(CommonUtils.priceFormat(subTotal));
                tvTax.setText(String.valueOf(CommonUtils.priceFormat(tax)));
                tvGrandTotal.setText(String.valueOf(CommonUtils.priceFormat(grandTotal)));
            }
        }
    }

    private void listCart(String customerId) {
        progressBarCart.setVisibility(View.VISIBLE);
        ApiInterface apiInterface = APIClient.getClient().create(ApiInterface.class);
        Call<CartServerDataItem> callApi = apiInterface.listCart(customerId);
        callApi.enqueue(new Callback<CartServerDataItem>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<CartServerDataItem> call, @NonNull Response<CartServerDataItem> response) {
                AppLogger.e(AppConstants.RESPONSE, "--------------" + response.body());
                progressBarCart.setVisibility(View.GONE);
                assert response.body() != null;

                if (response.body().getStatus().equalsIgnoreCase(AppConstants.STATUS_CODE_SUCCESS)) {
                    btnContinue.setEnabled(true);
                    grandTotal = response.body().getGrandtotal();
                    subTotal = response.body().getSubtotal();
                    tax = response.body().getTax();

                    tvGrandTotal.setText(CommonUtils.priceFormat(grandTotal));
                    tvSubTotal.setText(CommonUtils.priceFormat(subTotal));
                    tvTax.setText(CommonUtils.priceFormat(tax));

                    CartServerAdapter cartServerAdapter = new CartServerAdapter(getActivity(), response.body().getData(), ShoppingFrg.this);
                    recycleViewCart.setAdapter(cartServerAdapter);

                } else {
                    btnContinue.setEnabled(false);
                    linNoData.setVisibility(View.VISIBLE);
                    btnContinue.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public void onFailure(@NonNull Call<CartServerDataItem> call, @NonNull Throwable t) {
                progressBarCart.setVisibility(View.GONE);
                btnContinue.setEnabled(false);
                linNoData.setVisibility(View.VISIBLE);
            }

        });
    }

    public void updateCart(String customerId) {
        progressBarCart.setVisibility(View.VISIBLE);
        showProgressDialog("Cart","Updating Cart");
        ApiInterface apiInterface = APIClient.getClient().create(ApiInterface.class);
        Call<CartServerDataItem> callApi = apiInterface.listCart(customerId);
        callApi.enqueue(new Callback<CartServerDataItem>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<CartServerDataItem> call, @NonNull Response<CartServerDataItem> response) {
                AppLogger.e(AppConstants.RESPONSE, "--------------" + response.body());
                progressBarCart.setVisibility(View.GONE);
                assert response.body() != null;


                if (response.body().getStatus().equalsIgnoreCase(AppConstants.STATUS_CODE_SUCCESS)) {
                    hideProgressDialog();
                    grandTotal = response.body().getGrandtotal();
                    subTotal = response.body().getSubtotal();
                    tax = response.body().getTax();

                    tvGrandTotal.setText(CommonUtils.priceFormat(grandTotal));
                    tvSubTotal.setText(CommonUtils.priceFormat(subTotal));
                    tvTax.setText(CommonUtils.priceFormat(tax));
                    AppLogger.e("tax", "------------" + tax);

                }else{
                    hideProgressDialog();
                    btnContinue.setEnabled(false);
                    linNoData.setVisibility(View.VISIBLE);
                    btnContinue.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<CartServerDataItem> call, @NonNull Throwable t) {
                progressBarCart.setVisibility(View.GONE);
            }

        });
    }


}
