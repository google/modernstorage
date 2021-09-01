/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.modernstorage.sample.mediastore

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.modernstorage.mediastore.FileType
import com.google.modernstorage.mediastore.TakePicture
import com.google.modernstorage.mediastore.TakeVideo
import com.google.modernstorage.sample.R
import com.google.modernstorage.sample.databinding.FragmentMediastoreBinding

class AddMediaFragment : Fragment() {
    private var _binding: FragmentMediastoreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddMediaViewModel by viewModels()

    private val actionRequestPermission = registerForActivityResult(RequestPermission()) {
        handlePermissionSectionVisibility()
    }

    private val actionTakeImage = registerForActivityResult(TakePicture()) { success ->
        viewModel.onPhotoCapture(success)
    }

    private val actionTakeVideo = registerForActivityResult(TakeVideo()) { uri ->
        viewModel.onVideoCapture(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediastoreBinding.inflate(inflater, container, false)

        setupButtonListeners()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.addMedia.isEnabled = !isLoading
        }

        viewModel.currentFile.observe(viewLifecycleOwner) { currentMedia ->
            currentMedia ?: return@observe
            binding.details.text = currentMedia.toString()
        }

        viewModel.snackbarNotification.observe(viewLifecycleOwner) { text ->
            text ?: return@observe
            Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
            viewModel.clearSnackbarNotificationState()
        }

        viewModel.captureMediaIntent.observe(viewLifecycleOwner) { request ->
            request ?: return@observe

            when (request.type) {
                FileType.IMAGE -> {
                    viewModel.saveTemporaryCameraImageUri(request.uri)
                    actionTakeImage.launch(request.uri)
                }
                FileType.VIDEO -> actionTakeVideo.launch(request.uri)
                else -> throw IllegalArgumentException("Unsupported type: ${request.type}")
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handlePermissionSectionVisibility()
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearCaptureMediaIntentRequest()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handlePermissionSectionVisibility() {
        if (viewModel.canWriteInMediaStore) {
            binding.demoSection.visibility = View.VISIBLE
            binding.permissionSection.visibility = View.GONE
        } else {
            binding.demoSection.visibility = View.GONE
            binding.permissionSection.visibility = View.VISIBLE
        }
    }

    private fun setupButtonListeners() {
        binding.mediastoreType.clearChecked()
        binding.mediastoreSource.clearChecked()

        binding.mediastoreType.isSelectionRequired = true
        binding.mediastoreSource.isSelectionRequired = true

        binding.mediastoreType.check(R.id.type_image)
        binding.mediastoreSource.check(R.id.source_internet)

        binding.requestPermissionButton.setOnClickListener {
            actionRequestPermission.launch(WRITE_EXTERNAL_STORAGE)
        }

        binding.addMedia.setOnClickListener {
            onAddMediaClickListener()
        }
    }

    private fun onAddMediaClickListener() {
        val mediaType = when (binding.mediastoreType.checkedButtonId) {
            R.id.type_image -> FileType.IMAGE
            R.id.type_video -> FileType.VIDEO
            else -> throw Exception("This is not supposed to happen")
        }

        when (binding.mediastoreSource.checkedButtonId) {
            R.id.source_internet -> viewModel.saveRandomMediaFromInternet(mediaType)
            R.id.source_camera -> viewModel.captureMedia(mediaType)
            else -> throw IllegalArgumentException("Unsupported type: $mediaType")
        }
    }
}
