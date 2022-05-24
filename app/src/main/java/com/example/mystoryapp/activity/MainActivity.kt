package com.example.mystoryapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.R
import com.example.mystoryapp.customview.EmailEditText
import com.example.mystoryapp.customview.LoginButton
import com.example.mystoryapp.customview.PasswordEditText
import com.example.mystoryapp.customview.SignupButton
import com.example.mystoryapp.data.Session
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.response.LoginResponse
import com.example.mystoryapp.retrofit.ApiConfig
import com.example.mystoryapp.settings.SettingPreferences
import com.example.mystoryapp.settings.SettingPreferencesViewModel
import com.example.mystoryapp.settings.SettingPreferencesViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.datasore: DataStore<Preferences> by preferencesDataStore(name = "login")

class MainActivity : AppCompatActivity() {

    private var _activityMainBinding: ActivityMainBinding? = null
    private val binding get() = _activityMainBinding

    private lateinit var loginButton: LoginButton
    private lateinit var emailEditText: EmailEditText
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var signupButton: SignupButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        showLoading(false)

        loginButton = binding!!.loginButton
        emailEditText = binding!!.usernameEditText
        passwordEditText = binding!!.passwordEditText
        signupButton = binding!!.signupButton
        val showPasswordCheckbox = binding!!.passwordShowCheckbox


        val pref = SettingPreferences.getInstance(datasore)
        val settingPreferencesViewModel = ViewModelProvider(this, SettingPreferencesViewModelFactory(pref))[SettingPreferencesViewModel::class.java]

        settingPreferencesViewModel.getLoginSettings().observe(this
        ) { data: Session ->
            if (data.isLogin) {
                val intentStory = Intent(this@MainActivity, StoryActivity::class.java)
                startActivity(intentStory)
                finish()
            }
        }

        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                Toast.makeText(this, "Password Ditampilkan", Toast.LENGTH_SHORT).show()
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }else{
                Toast.makeText(this, "Password Disembunyikan", Toast.LENGTH_SHORT).show()
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        setLoginButtonEnable()

        init()

        loginButton.setOnClickListener {
            login()
        }

        signupButton.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        }

        val signupResponse: String? = intent.getStringExtra(EXTRA_RESPONSE_SIGNUP)
        if (signupResponse != null){
            Toast.makeText(this, signupResponse, Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(){
        showLoading(true)
        ApiConfig.getApiService().postUserLogin(
            emailEditText.text.toString().trim(),
            passwordEditText.text.toString().trim()
        ).enqueue(object : Callback<LoginResponse>{
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                showLoading(false)
                val loginResponse = response.body()
                if (loginResponse != null) {
                    postLoginToken(loginResponse, true)
                }

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun init(){
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setLoginButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setLoginButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun setLoginButtonEnable() {
        val username = emailEditText.text
        val password = passwordEditText.text
        loginButton.isEnabled = username != null && username.toString().isNotEmpty() && password != null && password.toString().length >= 6
    }

    private fun postLoginToken(data: LoginResponse, isLogin: Boolean){
        val pref = SettingPreferences.getInstance(datasore)
        val settingPreferencesViewModel = ViewModelProvider(this, SettingPreferencesViewModelFactory(pref))[SettingPreferencesViewModel::class.java]
        data.loginResult?.token?.let { settingPreferencesViewModel.saveLoginSetting(isLogin, it) }
    }

    private fun showLoading(isLoading: Boolean){
        if (isLoading){
            binding?.progressBar?.visibility  = View.VISIBLE
        } else {
            binding?.progressBar?.visibility  = View.GONE
        }
    }

    companion object{
        const val EXTRA_RESPONSE_SIGNUP = "extra_response_signup"
    }
}