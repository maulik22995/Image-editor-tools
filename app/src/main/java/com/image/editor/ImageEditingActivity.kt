package com.image.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.slider.Slider
import com.image.editor.databinding.ActivityImageEditingBinding
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class ImageEditingActivity : AppCompatActivity() {

    companion object {
        val KEY_SELECTED_IMAGE = "selected_image-path"
    }

    lateinit var binding: ActivityImageEditingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageEditingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val croppedOilPatternFilePath = intent.getStringExtra(KEY_SELECTED_IMAGE)
        val bitmap = uriToBitmap(Uri.parse(croppedOilPatternFilePath))


        binding.customView.post {
            bitmap?.let {
                binding.customView.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        it,
                        binding.customView.width / 2,
                        binding.customView.height / 2,
                        true
                    )
                )
            }
        }

        binding.btnSave.setOnClickListener {
            if (binding.customView.getCurrentMode() == CustomImageEditingView.Mode.CROP) {
                binding.customView.cropAndSetImage()
                resetAllMode()
                binding.btnMove.performClick()
            } else {
                saveImage()
            }
        }

        binding.sliderOpacity.addOnChangeListener { _, value, _ ->
            binding.customView.alpha = 1 - value
        }

        binding.sliderSkew.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(p0: Slider) {

            }

            override fun onStopTrackingTouch(p0: Slider) {
                binding.customView.skewImageUpdate()
            }
        })

        binding.sliderSkew.addOnChangeListener { _, value, _ ->
            Log.d("Value", "$value")
            if (binding.cbVertical.isChecked) {
                binding.customView.skewImage(0f, value, binding.cbTopBottom.isChecked)
            } else {
                binding.customView.skewImage(value, 0f, binding.cbTopBottom.isChecked)
            }
        }

        binding.btnFlip.setOnClickListener {
            resetAllMode()
            binding.btnFlip.setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)
            binding.btnFlip.setBackgroundColor(getColor(R.color.primary))
            binding.customView.flipHorizontally()
        }
        binding.btnResize.setOnClickListener {
            resetAllMode()
            binding.btnResize.setColorFilter(getColor(R.color.white))
            binding.btnResize.setBackgroundColor(getColor(R.color.primary))
            binding.customView.setMode(CustomImageEditingView.Mode.RESIZE)
        }
        binding.btnMove.setOnClickListener {
            resetAllMode()
            binding.btnMove.setColorFilter(getColor(R.color.white))
            binding.btnMove.setBackgroundColor(getColor(R.color.primary))
            binding.customView.setMode(CustomImageEditingView.Mode.DRAG)
        }
        binding.btnCrop.setOnClickListener {
            resetAllMode()
            binding.btnCrop.setColorFilter(getColor(R.color.white))
            binding.btnCrop.setBackgroundColor(getColor(R.color.primary))
            binding.customView.setMode(CustomImageEditingView.Mode.CROP)
            binding.btnSave.text = "Crop"
        }
        binding.btnSkew.setOnClickListener {
            resetAllMode()
            binding.groupSkew.visibility = View.VISIBLE
            binding.btnSkew.setColorFilter(getColor(R.color.white))
            binding.btnSkew.setBackgroundColor(getColor(R.color.primary))
            binding.customView.setMode(CustomImageEditingView.Mode.SKEW)
        }
        binding.btnAlpha.setOnClickListener {
            resetAllMode()
            binding.sliderOpacity.visibility = View.VISIBLE
            binding.btnAlpha.setColorFilter(getColor(R.color.white))
            binding.btnAlpha.setBackgroundColor(getColor(R.color.primary))
            binding.customView.setMode(CustomImageEditingView.Mode.ALPHA)
        }
    }

    private fun saveImage() {
        //preparing bitmap image
        binding.sliderOpacity.visibility = View.GONE
        binding.btnMove.performClick()
        try {
            val bitmap = binding.customView.getBitmap()
            if (bitmap != null) {
                // Crop the bitmap
                val path = saveBitmapAndGetPath(applicationContext, bitmap)
                if (path != null) {
                    val intent = Intent()
                    intent.putExtra("imagePath", path.toString())
                    setResult(RESULT_OK, intent)
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error while generation oil pattern", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    fun View.getBitmap(): Bitmap? {
        val b = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        layout(left, top, right, bottom)
        draw(c)
        return b
    }


    fun Activity.uriToBitmap(selectedFileUri: Uri): Bitmap? {
        return try {
            val parcelFileDescriptor: ParcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedFileUri, "r")!!
            val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            image
        } catch (e: IOException) {
            null
        }
    }

    fun saveBitmapAndGetPath(context: Context, bitmap: Bitmap): Uri? {
        val cacheDirectory = File(context.cacheDir, "images")
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }
        var bitmapPath: File? = null
        val fileName = UUID.randomUUID().toString() + ".png"
        try {
            bitmapPath = File(cacheDirectory, fileName)
            bitmapPath.createNewFile()
            val stream =
                FileOutputStream("$bitmapPath")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            Log.e("TAG", "saving bitmap error: $bitmap", e)
        }
        return bitmapPath?.toUri()
    }

    private fun resetAllMode() {
        binding.groupSkew.visibility = View.GONE
        binding.btnFlip.setColorFilter(getColor(R.color.black))
        binding.btnMove.setColorFilter(getColor(R.color.black))
        binding.btnSkew.setColorFilter(getColor(R.color.black))
        binding.btnResize.setColorFilter(getColor(R.color.black))
        binding.btnCrop.setColorFilter(getColor(R.color.black))
        binding.btnAlpha.setColorFilter(getColor(R.color.black))
        binding.btnFlip.background = null
        binding.btnMove.background = null
        binding.btnSkew.background = null
        binding.btnResize.background = null
        binding.btnCrop.background = null
        binding.btnAlpha.background = null
        binding.sliderOpacity.visibility = View.GONE
        binding.btnSave.text = "Save"
    }
}