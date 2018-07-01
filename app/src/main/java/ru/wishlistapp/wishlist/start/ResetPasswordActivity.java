package ru.wishlistapp.wishlist.start;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ResetPasswordActivity extends AppCompatActivity {
    TextView usernameEmail;
    Button button;
    String username, email;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        setTitle(R.string.restore);
        context = this;

        usernameEmail = (EditText) findViewById(R.id.forgotPasswordUsernameEmail);
        button = (Button) findViewById(R.id.forgotPasswordBtn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setClickable(false);
                if (isEmail()) {
                    username = null;
                    email = usernameEmail.getText().toString().trim();
                } else {
                    username = usernameEmail.getText().toString().trim();
                    email = null;
                }

                // Отправляем данные на сервер, там поиск пользователя
                // отправка письма при успешном поиске
                WishlistInterface apiService = ServiceGenerator.createService(WishlistInterface.class, BASE_URL);
                ApiClient.getClient().create(WishlistInterface.class);
                try {
                    Call<SuccessAndMessageModel> call = apiService.resetPasswordSendEmail(username, email);
                    call.enqueue(new Callback<SuccessAndMessageModel>() {
                        @Override
                        public void onResponse(Call<SuccessAndMessageModel> call, Response<SuccessAndMessageModel> response) {
                            AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(context);
                            alert.setTitle(usernameEmail.getText().toString());
                            if (response.body().getSuccess()) {
                                alert.setMessage(R.string.reset_password_mail_sent);
                                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                        Intent intent = new Intent(context, NewPasswordActivity.class);
                                        intent.putExtra("username", username);
                                        intent.putExtra("email", email);
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
                                button.setClickable(true);
                            }
                            AlertDialog alert1 = alert.create();
                            alert1.show();
                        }

                        @Override
                        public void onFailure(Call<SuccessAndMessageModel> call, Throwable t) {
                            Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                            button.setClickable(true);
                        }
                    });
                } catch (Exception e) {
                }

            }
        });
    }

    private boolean isEmail() {
        if (usernameEmail.getText().toString().contains("@")) return true;
        else return false;
    }
}

