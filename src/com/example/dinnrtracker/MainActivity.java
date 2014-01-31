package com.example.dinnrtracker;

import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

public class MainActivity extends Activity {

	private Context mContext;
    private Menu mMenu;

    private String mEmail;
    private String mPassword;

    private EditText mEmailView;
    private EditText mPasswordView;
    private TextView mLoginButton;
    
    private enum LoginAction {SIGNUP, LOGIN, RESET, SENT};
    private LoginAction mCurrentLoginAction;
    
    public static int sLoginOKCode = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this.getApplicationContext();
        mCurrentLoginAction = LoginAction.SIGNUP;
        
		Parse.initialize(this, "nZQDFNPlM4KDlQ4DwQZuorHU63Bpu00Kq40Ej3Z4", "fNqBIDZYQw1Up04NJ69zvk7F9ahBKTS74ffCYJVY");
		
        ParseAnalytics.trackAppOpened(getIntent());
        
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Intent TrackIntent = new Intent(this,TrackActivity.class);
            startActivity(TrackIntent);
        }
        
        TextView textView = (EditText) findViewById(R.id.loginEmailField);
        mEmailView = (EditText) textView;
        textView = (EditText) findViewById(R.id.loginPasswordField);
        mPasswordView = (EditText) textView;
        textView = (TextView) findViewById(R.id.loginButton);
        mLoginButton = textView;
                
        mPasswordView
        .setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                    KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    buttonClicked();
                    return true;
                }
                return false;
            }
        });
		mLoginButton.setOnClickListener(
		        new View.OnClickListener() {
		            @Override
		            public void onClick(View view) {
		                buttonClicked();
		            }
		        });
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        mMenu = menu;
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.actionLogin) {
			if (item.getTitle().toString().equals((getString(R.string.action_login)))) {
			    switchToAction(LoginAction.LOGIN);
			} else {
			    switchToAction(LoginAction.SIGNUP);
			}
			return true;
		} else if (itemId == R.id.actionPassword) {
			switchToAction(LoginAction.RESET);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
    
	public void buttonClicked() {
        if (!loginInputsHasErrors()) {
            InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);     
            /*
        	if (mCurrentLoginAction != LoginAction.SENT && NetworkUtil.checkNetworkStatus(mContext) == NetworkUtil.TYPE_NOT_CONNECTED) {
        		return;
        	}*/
            
            if (mCurrentLoginAction == LoginAction.SIGNUP) {
                attemptSignup();
            } else if (mCurrentLoginAction == LoginAction.LOGIN) {
                attemptLogin();
            } else if (mCurrentLoginAction == LoginAction.RESET) {
                sendResetInstruction();
            } else { // mCurrentLoginAction == LoginAction.SENT
                switchToAction(LoginAction.LOGIN);
            }
        }
    }
	
	private boolean loginInputsHasErrors() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store inputs
        mEmail = mEmailView.getText().toString().toLowerCase(Locale.ENGLISH);
        mPassword = mPasswordView.getText().toString();

        boolean hasErrors = false;
        View focusView = null;

        // Check for a valid password.
        if (mCurrentLoginAction != LoginAction.RESET && mCurrentLoginAction != LoginAction.SENT && TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.login_empty_password_error));
            focusView = mPasswordView;
            hasErrors = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.login_empty_email_error));
            focusView = mEmailView;
            hasErrors = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.login_invalid_email_error));
            focusView = mEmailView;
            hasErrors = true;
        }

        if (focusView != null) {
            focusView.requestFocus();
        }
        
        return hasErrors;
    }
	
	private void attemptSignup() {
        
        ParseUser user = new ParseUser();
        user.setUsername(mEmail);
        user.setPassword(mPassword);
        user.setEmail(mEmail);
        
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Intent TrackIntent = new Intent(mContext, TrackActivity.class);
                    startActivity(TrackIntent);
                } else {
                    switch (e.getCode()) {
                        case ParseException.EMAIL_TAKEN:
                        case ParseException.USERNAME_TAKEN:
                        case ParseException.INVALID_EMAIL_ADDRESS:
                            mEmailView.setError(e.getMessage());
                            mEmailView.requestFocus();
                            break;
                        default:
                            Toast.makeText(mContext, "Oops, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        });
    }
    
    private void attemptLogin() {
        
        ParseUser.logInInBackground(mEmail, mPassword, new LogInCallback() {
              public void done(ParseUser user, ParseException e) {
                  if (user != null) {
                      Intent TrackIntent = new Intent(mContext, TrackActivity.class);
                      startActivity(TrackIntent);
                  } else {
                      switch (e.getCode()) {
                        case ParseException.INVALID_EMAIL_ADDRESS:
                            mEmailView.setError(e.getMessage());
                            mEmailView.requestFocus();
                            break;
                        case ParseException.OBJECT_NOT_FOUND:
                            mPasswordView.setError(e.getMessage());
                            mPasswordView.requestFocus();
                            break;
                        default:
                            Toast.makeText(mContext, "Oops, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                            break;
                      }
                  }
              }
        });
    }
    
    private void sendResetInstruction() {
        
        ParseUser.requestPasswordResetInBackground(mEmail,
                new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            switchToAction(LoginAction.SENT);
                        } else {
                        	switch (e.getCode()) {
                            	case ParseException.INVALID_EMAIL_ADDRESS:
                            	case ParseException.EMAIL_NOT_FOUND:
                            		mEmailView.setError(e.getMessage());
                            		mEmailView.requestFocus();
                            		break;
                            	default:
                                    Toast.makeText(mContext, "Oops, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                                    break;
                        	}
                        }
                    }
        });
    }
    
    private void switchToAction(LoginAction loginAction) {
        MenuItem item;
        switch (loginAction) {
	        case SIGNUP:
	            mCurrentLoginAction = LoginAction.SIGNUP;
	            mLoginButton.setText(getString(R.string.action_signup));
	            mPasswordView.setVisibility(View.VISIBLE);
	            item = mMenu.findItem(R.id.actionLogin);
	            item.setTitle(getString(R.string.action_login));
	            break;
	        case LOGIN:
	            mCurrentLoginAction = LoginAction.LOGIN;
	            mLoginButton.setText(getString(R.string.action_login));
	            mPasswordView.setVisibility(View.VISIBLE);
	            item = mMenu.findItem(R.id.actionLogin);
	            item.setTitle(getString(R.string.action_signup));
	            break;
	        case RESET:
	            mCurrentLoginAction = LoginAction.RESET;
	            mLoginButton.setText(getString(R.string.login_reset_button));
	            mPasswordView.setVisibility(View.GONE);
	            item = mMenu.findItem(R.id.actionLogin);
	            item.setTitle(getString(R.string.action_login));
	            break;
	        default: // SENT
	            mCurrentLoginAction = LoginAction.SENT;
	            mLoginButton.setText(getString(R.string.login_sent_button));
	            mPasswordView.setVisibility(View.GONE);
	            item = mMenu.findItem(R.id.actionLogin);
	            item.setTitle(getString(R.string.action_login));
	            break;
        }
    }
	
}