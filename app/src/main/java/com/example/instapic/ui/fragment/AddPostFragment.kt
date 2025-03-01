package com.example.instapic.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.instapic.databinding.FragmentAddPostBinding
import com.example.instapic.model.PostModel
import com.example.instapic.repository.PostRepositoryImpl
import com.example.instapic.utils.CloudinaryHelper
import com.example.instapic.utils.LoadingUtils
import com.example.instapic.viewmodel.PostViewModel
import com.example.instapic.viewmodel.PostViewModelFactory

class AddPostFragment : Fragment() {
    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private lateinit var loadingUtils: LoadingUtils
    private val tag = "AddPostFragment"

    private val postViewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepositoryImpl())
    }

    // Image picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        Log.d(tag, "Image picker result: $uri")
        if (uri != null) {
            try {
                selectedImageUri = uri
                binding.imagePreview.setImageURI(uri)
                Log.d(tag, "Image set successfully")
            } catch (e: Exception) {
                Log.e(tag, "Error setting image: ${e.message}")
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(tag, "No image selected")
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingUtils = LoadingUtils(requireActivity())

        // Select image button
        binding.selectImageButton.setOnClickListener {
            Log.d(tag, "Select image button clicked")
            try {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (e: Exception) {
                Log.e(tag, "Error launching image picker: ${e.message}")
                Toast.makeText(requireContext(), "Error opening image picker", Toast.LENGTH_SHORT).show()
            }
        }

        // Upload button
        binding.uploadButton.setOnClickListener {
            uploadPost()
        }

        // Observe upload status
        postViewModel.uploadStatus.observe(viewLifecycleOwner) { (success, message) ->
            Log.d(tag, "Upload status: success=$success, message=$message")
            loadingUtils.dismiss()
            binding.uploadButton.isEnabled = true
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (success) {
                clearForm()
            }
        }
    }

    private fun uploadPost() {
        Log.d(tag, "Starting upload process")
        
        if (selectedImageUri == null) {
            Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        val caption = binding.captionInput.text.toString()
        if (caption.isEmpty()) {
            Toast.makeText(context, "Please write a caption", Toast.LENGTH_SHORT).show()
            return
        }

        binding.uploadButton.isEnabled = false
        loadingUtils.show()

        selectedImageUri?.let { uri ->
            Log.d(tag, "Uploading image to Cloudinary: $uri")
            CloudinaryHelper.uploadImage(uri) { imageUrl, error ->
                Log.d(tag, "Cloudinary response - URL: $imageUrl, Error: $error")
                activity?.runOnUiThread {
                    if (imageUrl != null) {
                        val post = PostModel(
                            userId = postViewModel.getCurrentUserId(),
                            imageUrl = imageUrl,
                            caption = caption,
                            timestamp = System.currentTimeMillis()
                        )
                        postViewModel.uploadPost(post)
                    } else {
                        loadingUtils.dismiss()
                        binding.uploadButton.isEnabled = true
                        Toast.makeText(context, "Failed to upload image: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun clearForm() {
        binding.captionInput.text.clear()
        binding.imagePreview.setImageDrawable(null)
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
