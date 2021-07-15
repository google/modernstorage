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
package com.google.modernstorage.sample.filesystem

import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.modernstorage.sample.R
import com.google.modernstorage.sample.databinding.FragmentFilesystemBinding

@RequiresApi(Build.VERSION_CODES.O)
class FileSystemFragment : Fragment() {
    private var _binding: FragmentFilesystemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FileSystemViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesystemBinding.inflate(inflater, container, false)

        setupLayout()

        viewModel.currentFile.observe(viewLifecycleOwner) { currentFile ->
            binding.fileSizeAndMimeType.text = getString(
                R.string.filesystem_size,
                Formatter.formatShortFileSize(context, currentFile.size)
            )
            binding.fileUri.text = currentFile.uri.toString()
        }

        viewModel.currentFileContent.observe(viewLifecycleOwner) { fileContent ->
            val contentView = when (fileContent) {
                is TextContent -> TextView(context).apply {
                    text = fileContent.value
                }
                is BitmapContent -> ImageView(context).apply {
                    this.setImageBitmap(fileContent.value)
                }
            }

            binding.filePreview.removeAllViews()
            binding.filePreview.addView(contentView)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLayout() {
        binding.openTextFile.setOnClickListener {
            actionOpenTextFile.launch(arrayOf("text/*"))
        }

        binding.createTextFile.setOnClickListener { button ->
            Snackbar.make(button, R.string.filesystem_not_available_yet, Snackbar.LENGTH_SHORT)
                .show()
        }

        binding.openImageFile.setOnClickListener {
            actionOpenImageFile.launch(arrayOf("image/*"))
        }
    }

    private val actionOpenTextFile = registerForActivityResult(OpenDocument()) { uri ->
        uri?.let { viewModel.onFileSelect(it, FileType.TEXT) }
    }

    private val actionOpenImageFile = registerForActivityResult(OpenDocument()) { uri ->
        uri?.let { viewModel.onFileSelect(it, FileType.IMAGE) }
    }
}
