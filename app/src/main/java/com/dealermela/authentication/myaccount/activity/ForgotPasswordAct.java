package com.dealermela.authentication.myaccount.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.dealermela.DealerMelaBaseActivity;
import com.dealermela.R;
import com.dealermela.authentication.myaccount.dialog.LogoutDialogClass;
import com.dealermela.authentication.myaccount.dialog.SuccessDialogClass;
import com.dealermela.home.activity.MainActivity;
import com.dealermela.retrofit.APIClient;
import com.dealermela.retrofit.ApiInterface;
import com.dealermela.util.AppConstants;
import com.dealermela.util.AppLogger;
import com.dealermela.util.CommonUtils;
import com.dealermela.util.Validator;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordAct extends DealerMelaBaseActivity {

    private EditText edEmail;
    private Button btnResetPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.act_forgot_password;
    }

    @Override
    public void init() {

    }

    @Override
    public void initView() {
        bindToolBar(getString(R.string.forgot_password));
        edEmail = findViewById(R.id.edEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addListener() {
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = validateData();
                if (valid) {
                    forgotPassword(edEmail.getText().toString());
                }
            }
        });
    }

    @Override
    public void loadData() {

    }

    private void forgotPassword(String email) {
        showProgressDialog(getString(R.string.forgot_password), getString(R.string.please_wait));
        ApiInterface apiInterface = APIClient.getClient().create(ApiInterface.class);
        Call<JsonObject> callApi = apiInterface.forgotPassword(email);
        callApi.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Log.e(AppConstants.RESPONSE, "-----------------" + response.body());
                assert response.body() != null;
                hideProgressDialog();
                String status=null,message = null;
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(String.valueOf(response.body()));
                        status = jsonObject.getString("status");
                        message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase(AppConstants.STATUS_CODE_SUCCESS)) {
                            SuccessDialogClass successDialogClass = new SuccessDialogClass(ForgotPasswordAct.this,message);
                            successDialogClass.setCancelable(false);
                            successDialogClass.show();
                            Objects.requireNonNull(successDialogClass.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                            Objects.requireNonNull(successDialogClass.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        }else{
                            new AlertDialog.Builder(ForgotPasswordAct.this,R.style.AppCompatAlertDialogStyle)
                                    .setTitle(CommonUtils.capitalizeString(status))
                                    .setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                AppLogger.e(TAG, "------------" + t.getMessage());
                hideProgressDialog();
            }

        });
    }

    private boolean validateData() {
        if (!Validator.checkEmpty(edEmail, getString(R.string.login_please_enter_email))) {
            return false;
        } else if (!Validator.checkEmail(edEmail)) {
            edEmail.setError(getString(R.string.login_please_enter_valid_email));
            return false;
        }
        return true;
    }
}
