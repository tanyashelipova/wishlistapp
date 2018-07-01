package ru.wishlistapp.wishlist.start;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wishlistapp.wishlist.R;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.server.ApiClient;
import ru.wishlistapp.wishlist.server.ServiceGenerator;
import ru.wishlistapp.wishlist.server.WishlistInterface;

import static ru.wishlistapp.wishlist.server.ApiClient.BASE_URL;

public class NewPasswordActivity extends AppCompatActivity {

    Button mButton;
    TextView mPassword2View, mPasswordView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        setTitle(R.string.restore);

        mPasswordView = (EditText) findViewById(R.id.newPassword);
        mPassword2View = (EditText) findViewById(R.id.newPassword2);
        mButton = (Button) findViewById(R.id.buttonNewPassword);
        context = this;

        Intent intent = getIntent();
        final String email = intent.getStringExtra("email");
        final String username = intent.getStringExtra("username");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPassword()) {
                    mButton.setClickable(false);
                    String password = mPasswordView.getText().toString();
                    WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
                    ApiClient.getClient().create(WishlistInterface.class);
                    try {
                        Call<SuccessAndMessageModel> call = apiService.restorePassword(username, email, password);
                        call.enqueue(new Callback<SuccessAndMessageModel>() {
                            @Override
                            public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                                AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(context);
                                if (response.body().getSuccess()) {
                                    alert.setMessage(response.body().getMessage());
                                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                            Intent intent = new Intent(context, LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                } else {
                                    alert.setMessage(response.body().getMessage());
                                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                }
                                AlertDialog alert1 = alert.create();
                                alert1.show();
                                mButton.setClickable(true);
                            }

                            @Override
                            public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                                Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                                mButton.setClickable(true);
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    private boolean checkPassword() {
        if (TextUtils.isEmpty(mPasswordView.getText())) {
            mPasswordView.setError(getText(R.string.error_field_required));
            return false;
        }
        if (TextUtils.isEmpty(mPassword2View.getText())) {
            mPassword2View.setError(getText(R.string.error_field_required));
            return false;
        }
        if (!TextUtils.equals(mPasswordView.getText(), mPassword2View.getText())) {
            mPassword2View.setError(getText(R.string.error_password_mismatch));
            return false;
        }
        if (mPasswordView.getText().toString().length() < 8) {
            mPasswordView.setError(getText(R.string.error_invalid_password));
            return false;
        }
        return true;
    }
}
