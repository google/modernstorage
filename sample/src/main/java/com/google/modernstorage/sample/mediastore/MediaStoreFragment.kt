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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.modernstorage.mediastore.CustomTakePicture
import com.google.modernstorage.mediastore.CustomTakeVideo
import com.google.modernstorage.sample.R
import com.google.modernstorage.sample.databinding.FragmentMediastoreBinding

class MediaStoreFragment : Fragment() {
    private var _binding: FragmentMediastoreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MediaStoreViewModel by viewModels()

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

        viewModel.currentMedia.observe(viewLifecycleOwner) { currentMedia ->
            currentMedia ?: return@observe
            binding.details.text = currentMedia.toString()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handlePermissionSectionVisibility()
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
        binding.requestPermissionButton.setOnClickListener {
            actionRequestPermission.launch(WRITE_EXTERNAL_STORAGE)
        }

        binding.mediastoreType.clearChecked()
        binding.mediastoreSource.clearChecked()

        binding.mediastoreType.isSelectionRequired = true
        binding.mediastoreSource.isSelectionRequired = true

        binding.mediastoreType.check(R.id.type_image)
        binding.mediastoreSource.check(R.id.source_internet)

        binding.addMedia.setOnClickListener {
            val type = when (binding.mediastoreType.checkedButtonId) {
                R.id.type_image -> MediaType.IMAGE
                R.id.type_video -> MediaType.VIDEO
                else -> throw Exception("This is not supposed to happen")
            }

            when (binding.mediastoreSource.checkedButtonId) {
                R.id.source_internet -> downloadMedia(type)
                R.id.source_camera -> captureMedia(type)
                else -> throw Exception("This is not supposed to happen")
            }
        }
    }

    private fun downloadMedia(type: MediaType) {
        viewModel.setLoadingStatus(true)

        when (type) {
            MediaType.IMAGE -> {

                viewModel.saveRandomImageFromInternet { imageUri ->
                    viewModel.setLoadingStatus(false)
                    viewModel.setCurrentMedia(imageUri)
                }
            }
            MediaType.VIDEO -> viewModel.saveRandomVideoFromInternet { videoUri ->
                viewModel.setLoadingStatus(false)
                viewModel.setCurrentMedia(videoUri)
            }
        }
    }

    private fun captureMedia(type: MediaType) {
        viewModel.setLoadingStatus(true)

        viewModel.createMediaUriForCamera(type) { uri ->
            when (type) {
                MediaType.IMAGE -> {
                    viewModel.saveTemporaryCameraImageUri(uri)
                    actionTakeImage.launch(uri)
                }
                MediaType.VIDEO -> actionTakeVideo.launch(uri)
            }
        }
    }

    private val actionRequestPermission = registerForActivityResult(RequestPermission()) {
        handlePermissionSectionVisibility()
    }

    private val actionTakeImage = registerForActivityResult(CustomTakePicture()) { success ->
        viewModel.setLoadingStatus(false)

        if (!success) {
            Log.e(tag, "Image taken FAIL")
            return@registerForActivityResult
        }

        Log.d(tag, "Image taken SUCCESS")

        if (viewModel.temporaryCameraImageUri == null) {
            Log.e(tag, "Can't find previously saved temporary Camera Image URI")
        } else {
            viewModel.setCurrentMedia(viewModel.temporaryCameraImageUri!!)
            viewModel.clearTemporaryCameraImageUri()
        }
    }

    private val actionTakeVideo = registerForActivityResult(CustomTakeVideo()) { uri ->
        viewModel.setLoadingStatus(false)

        if (uri == null) {
            Log.e(tag, "Video taken FAIL")
            return@registerForActivityResult
        }

        Log.d(tag, "Video taken SUCCESS")
        viewModel.setCurrentMedia(uri)
    }
}
