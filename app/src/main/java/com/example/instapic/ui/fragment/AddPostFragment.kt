package com.example.instapic.ui.fragment

import android.net.Uri
import android.os.Build
import android.os.Bundle
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

    private val postViewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepositoryImpl())
    }

    // ✅ Register Photo Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.imagePreview.setImageURI(uri)
        } else {
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

        binding.selectImageButton.setOnClickListener {
            openImagePicker()
        }

        binding.uploadButton.setOnClickListener {
            uploadPost()
        }
    }

    private fun openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) // ✅ Works fine

        } else {
            // Use legacy file picker if needed
            Toast.makeText(requireContext(), "This device does not support the modern picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPost() {
        if (selectedImageUri == null) {
            Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.uploadButton.isEnabled = false
        loadingUtils.show()

        val caption = binding.captionInput.text.toString()
        selectedImageUri?.let { uri ->
            CloudinaryHelper.uploadImage(uri) { imageUrl, error ->
                activity?.runOnUiThread {
                    loadingUtils.dismiss()
                    binding.uploadButton.isEnabled = true

                    if (imageUrl != null) {
                        val post = PostModel(
                            userId = postViewModel.getCurrentUserId(),
                            imageUrl = imageUrl,
                            caption = caption,
                            timestamp = System.currentTimeMillis()
                        )
                        postViewModel.uploadPost(post)
                        Toast.makeText(context, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                        clearForm()
                    } else {
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
