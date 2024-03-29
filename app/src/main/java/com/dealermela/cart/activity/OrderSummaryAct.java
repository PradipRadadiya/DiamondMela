package com.dealermela.cart.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.dealermela.DealerMelaBaseActivity;
import com.dealermela.R;
import com.dealermela.authentication.myaccount.activity.ForgotPasswordAct;
import com.dealermela.authentication.myaccount.dialog.SuccessDialogClass;
import com.dealermela.authentication.myaccount.dialog.SuccessOrderDialogClass;
import com.dealermela.authentication.myaccount.model.LoginResponse;
import com.dealermela.cart.adapter.OrderSummaryAdapter;
import com.dealermela.cart.model.OrderSummaryItem;
import com.dealermela.ccavenue.activity.WebViewActivity;
import com.dealermela.ccavenue.utility.AvenuesParams;
import com.dealermela.ccavenue.utility.ServiceUtility;
import com.dealermela.retrofit.APIClient;
import com.dealermela.retrofit.ApiInterface;
import com.dealermela.util.AppConstants;
import com.dealermela.util.AppLogger;
import com.dealermela.util.CommonUtils;
import com.dealermela.util.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.dealermela.home.activity.MainActivity.customerId;

public class OrderSummaryAct extends DealerMelaBaseActivity implements View.OnClickListener {
    private RecyclerView recycleViewOrderSummary;
    private Button btnPlaceOrder;
    private TextView tvDefaultBillingAddress, tvDefaultShippingAddress, tvPaymentMethod, tvSubTotal, tvShippingCharge, tvTax, tvGrandTotal;
    private SharedPreferences sharedPreferences;
    private String total,orderId;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.act_order_summary;
    }

    @Override
    public void init() {
        Integer randomNum = ServiceUtility.randInt(0, 9999999);
        orderId=String.valueOf(randomNum.toString());
        sharedPreferences = new SharedPreferences(OrderSummaryAct.this);
    }

    @Override
    public void initView() {
        bindToolBar("Order Summary");
        recycleViewOrderSummary = findViewById(R.id.recycleViewOrderSummary);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        tvDefaultBillingAddress = findViewById(R.id.tvDefaultBillingAddress);
        tvDefaultShippingAddress = findViewById(R.id.tvDefaultShippingAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);

        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvShippingCharge = findViewById(R.id.tvShippingCharge);
        tvTax = findViewById(R.id.tvTax);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void postInitView() {
        tvDefaultBillingAddress.setText(sharedPreferences.getBillingAddress());
        tvDefaultShippingAddress.setText(sharedPreferences.getShippingAddress());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OrderSummaryAct.this);
        recycleViewOrderSummary.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void addListener() {
        btnPlaceOrder.setOnClickListener(this);
    }

    @Override
    public void loadData() {
        orderSummary();
    }

    private void orderSummary() {
        showProgressDialog("Order Summary", getString(R.string.please_wait));
        ApiInterface apiInterface = APIClient.getClient().create(ApiInterface.class);
        Call<OrderSummaryItem> callApi = apiInterface.orderSummary(customerId);
        callApi.enqueue(new Callback<OrderSummaryItem>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<OrderSummaryItem> call, @NonNull Response<OrderSummaryItem> response) {
                AppLogger.e(AppConstants.RESPONSE, "--------------" + response.body());
                hideProgressDialog();
                assert response.body() != null;
                hideProgressDialog();
                if (response.body().getStatus().equalsIgnoreCase(AppConstants.STATUS_CODE_SUCCESS)) {

                    tvPaymentMethod.setText(response.body().getPayment());


                    float subtotal = response.body().getSubtotal();
                    float grand_total = response.body().getGrandTotal();
                    float tax_ammount = response.body().getTaxAmmount();
                    float shipping_charges = response.body().getShippingCharges();

                    tvSubTotal.setText(CommonUtils.priceFormat(subtotal));
                    tvGrandTotal.setText(CommonUtils.priceFormat(grand_total));
                    total=String.valueOf(response.body().getGrandTotal());

                    tvTax.setText(CommonUtils.priceFormat(tax_ammount));
                    tvShippingCharge.setText(CommonUtils.priceFormat(shipping_charges));

                    OrderSummaryAdapter orderSummaryAdapter = new OrderSummaryAdapter(OrderSummaryAct.this, response.body().getData());
                    recycleViewOrderSummary.setAdapter(orderSummaryAdapter);


                }

            }

            @Override
            public void onFailure(@NonNull Call<OrderSummaryItem> call, @NonNull Throwable t) {
                hideProgressDialog();
            }

        });
    }

    private void placeOrder() {
        showProgressDialog("Order", getString(R.string.please_wait));
        ApiInterface apiInterface = APIClient.getClient().create(ApiInterface.class);
        Call<JsonObject> callApi = apiInterface.placeOrder(customerId);
        callApi.enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                AppLogger.e(AppConstants.RESPONSE, "--------------" + response.body());
                hideProgressDialog();
                assert response.body() != null;
                hideProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    if (jsonObject.getString("status").equalsIgnoreCase(AppConstants.STATUS_CODE_SUCCESS)) {
                        SuccessOrderDialogClass successDialogClass = new SuccessOrderDialogClass(OrderSummaryAct.this, jsonObject.getString("message"));
                        successDialogClass.setCancelable(false);
                        successDialogClass.show();
                        Objects.requireNonNull(successDialogClass.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                        Objects.requireNonNull(successDialogClass.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        cartCount = 0;

                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                hideProgressDialog();
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlaceOrder:
                if (tvPaymentMethod.getText().toString().equalsIgnoreCase("NEFT OR RTGS")){
                    placeOrder();
                }else{

                    String vAccessCode = ServiceUtility.chkNull(AppConstants.ACCESS_CODE).toString().trim();
                    String vMerchantId = ServiceUtility.chkNull(AppConstants.MERCHANT_ID).toString().trim();
                    String vCurrency = ServiceUtility.chkNull(AppConstants.CURRENCY).toString().trim();
                    String vAmount = ServiceUtility.chkNull(total).toString().trim();

                    if(!vAccessCode.equals("") && !vMerchantId.equals("") && !vCurrency.equals("") && !vAmount.equals("")){
                        Intent intent = new Intent(this, WebViewActivity.class);
                        intent.putExtra(AvenuesParams.ACCESS_CODE, ServiceUtility.chkNull(AppConstants.ACCESS_CODE).toString().trim());
                        intent.putExtra(AvenuesParams.MERCHANT_ID, ServiceUtility.chkNull(AppConstants.MERCHANT_ID).toString().trim());
                        intent.putExtra(AvenuesParams.ORDER_ID, ServiceUtility.chkNull(orderId).toString().trim());
                        intent.putExtra(AvenuesParams.CURRENCY, ServiceUtility.chkNull(AppConstants.CURRENCY).toString().trim());
                        intent.putExtra(AvenuesParams.AMOUNT, ServiceUtility.chkNull(total).toString().trim());

                        intent.putExtra(AvenuesParams.REDIRECT_URL, ServiceUtility.chkNull(AppConstants.redirectUrl).toString().trim());
                        intent.putExtra(AvenuesParams.CANCEL_URL, ServiceUtility.chkNull(AppConstants.cancelUrl).toString().trim());
                        intent.putExtra(AvenuesParams.RSA_KEY_URL, ServiceUtility.chkNull(AppConstants.rsaKeyUrl).toString().trim());
                        startActivity(intent);
                    }

                }

                break;
        }
    }

    //Option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        if (id == R.id.action_cart) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
