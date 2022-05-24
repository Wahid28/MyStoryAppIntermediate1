package com.example.mystoryapp.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mystoryapp.utils.createCustomTempFile
import com.example.mystoryapp.databinding.ActivityMainCameraBinding
import com.example.mystoryapp.utils.reduceFileImage
import com.example.mystoryapp.response.ImageUploadResponse
import com.example.mystoryapp.retrofit.ApiConfig
import com.example.mystoryapp.utils.rotateBitmap
import com.example.mystoryapp.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File

class MainCameraActivity : AppCompatActivity() {
    private var _activityMainCameraBinding: ActivityMainCameraBinding? = null
    private val binding get() = _activityMainCameraBinding

    private lateinit var currentPhotoPath: String
    private var getFile: File? = null

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == CAMERA_X_RESULT){
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it?.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile
            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )

            binding?.previewImageView?.setImageBitmap(result)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == RESULT_OK){
            val myFile = File(currentPhotoPath)
            getFile = myFile

            val result = BitmapFactory.decodeFile(myFile.path)

            binding?.previewImageView?.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK){
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            getFile = myFile

            binding?.previewImageView?.setImageURI(selectedImg)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityMainCameraBinding = ActivityMainCameraBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        showLoading(false)

        val token = intent.getStringExtra(EXTRA_POST_STORY)

        if (token != null) {
            Log.d(TAG, token)
        }else{
            Log.d(TAG, "Token: Null")
        }

        val descriptionEditText = binding?.storyDescription

        binding?.cameraXButton?.setOnClickListener { startCameraX() }
        binding?.cameraButton?.setOnClickListener { startTakePhoto() }
        binding?.galleryButton?.setOnClickListener { startGallery() }
        binding?.uploadButton?.setOnClickListener {
            if (token != null) {
                uploadImage(token, descriptionEditText?.text.toString())
            }
        }

        if (!allPermissionsGranted()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun uploadImage(token: String, descriptionBindingText: String) {
        showLoading(true)
        if (getFile != null){
            val file = reduceFileImage(getFile as File)

            val description = descriptionBindingText.toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            val bearerToken = "Bearer $token"

            val service = ApiConfig.getApiService().uploadImage(bearerToken, imageMultiPart, description)
            service.enqueue(object : Callback<ImageUploadResponse> {
                override fun onResponse(
                    call: Call<ImageUploadResponse>,
                    response: retrofit2.Response<ImageUploadResponse>
                ) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            Toast.makeText(this@MainCameraActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainCameraActivity, StoryActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@MainCameraActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@MainCameraActivity, "Gagal instance Retrofit", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        } else{
            showLoading(false)
            Toast.makeText(this@MainCameraActivity, "Silakan masukkan berkas gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also{
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.example.mystoryapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (!allPermissionsGranted()){
                Toast.makeText(this, "Tidak mendapatkan permission.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showLoading(isLoading: Boolean){
        if (isLoading){
            binding?.progressBar?.visibility  = View.VISIBLE
        } else {
            binding?.progressBar?.visibility  = View.GONE
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, StoryActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object{
        const val TAG = "MainCameraActivity"
        const val EXTRA_POST_STORY = "token"

        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}