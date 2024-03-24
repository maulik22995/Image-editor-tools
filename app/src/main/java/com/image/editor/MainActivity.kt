package com.image.editor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.image.editor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val startForImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                takeUriPermission(data?.data?.toString())
                val selectedImage = data?.data?.toString()
                val intent = Intent(this, ImageEditingActivity::class.java)
                intent.putExtra(ImageEditingActivity.KEY_SELECTED_IMAGE, selectedImage)
                startForPreviewResult.launch(intent)
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private val startForPreviewResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK) {
                val imagePath = data?.getStringExtra("imagePath")
                if(imagePath != null){
                    binding.ivEditedImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    if (imagePath.isNotEmpty()) {
                        binding.ivEditedImage.setImageURI(Uri.parse(imagePath))
                    }
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnChooseImage.setOnClickListener {
            galleryIntent()
        }
    }

    private fun galleryIntent() {
        val intent = Intent()
        intent.apply {
            type = "image/*"
            action = Intent.ACTION_OPEN_DOCUMENT
            flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startForImageFromGalleryResult.launch(
            Intent.createChooser(
                intent,
                "Select Image"
            )
        )
    }

    fun Activity.takeUriPermission(uri: String?) {
        if (uri.isNullOrEmpty()) return
        try {
            contentResolver?.takePersistableUriPermission(
                Uri.parse(uri),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (securityException: SecurityException) {
            Log.e(
                "No URI grants given for uri- ($uri). ",
                "Proceeding to view image without uri permissions grants"
            )
        }
    }
}