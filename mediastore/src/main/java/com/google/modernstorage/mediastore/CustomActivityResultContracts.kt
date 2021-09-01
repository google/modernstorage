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
package com.google.modernstorage.mediastore

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult

/**
 * An [ActivityResultContract] to take a picture using the [MediaStore.ACTION_IMAGE_CAPTURE] saving
 * it into the provided content [Uri]
 *
 * @return `true` if the image was saved into the given content [Uri]
 */
class TakePicture : ActivityResultContract<Uri, Boolean>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == Activity.RESULT_OK
}

/**
 * An [ActivityResultContract] to take a video using the [MediaStore.ACTION_VIDEO_CAPTURE] saving
 * it into the provided content [Uri]
 *
 * @return the content [Uri] if the video was saved into it otherwise returns `null`
 */
class TakeVideo : ActivityResultContract<Uri, Uri?>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) {
            null
        } else {
            intent.data
        }
    }
}

/**
 * An [ActivityResultContract] to delete a shared [FileResource]
 *
 * @return a successful [Result] without value if the [FileResource] has been deleted. A failed
 * [Result] means the system couldn't delete the file or the user denied the request (from API 30+)
 */
class DeleteFileResource : ActivityResultContract<FileResource, Result<Unit>>() {

    override fun createIntent(context: Context, input: FileResource): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST)
                .putExtra(
                    StartIntentSenderForResult.EXTRA_INTENT_SENDER_REQUEST,
                    IntentSenderRequest.Builder(
                        MediaStore.createDeleteRequest(
                            context.contentResolver,
                            listOf(input.uri)
                        ).intentSender
                    ).build()
                )
        } else {
            Intent()
        }
    }

    override fun getSynchronousResult(context: Context, input: FileResource):
        SynchronousResult<Result<Unit>>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            null
        } else {
            return try {
                when (context.contentResolver.delete(input.uri, null, null)) {
                    1 -> SynchronousResult(Result.success(Unit))
                    0 -> SynchronousResult(Result.failure(Exceptions.UriNotFoundException(input.uri)))
                    else -> SynchronousResult(
                        Result.failure(
                            Exceptions.UriNotDeletedException
                            (input.uri)
                        )
                    )
                }
            } catch (e: IllegalArgumentException) {
                SynchronousResult(Result.failure(e))
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result<Unit> {
        return if (resultCode == Activity.RESULT_OK) {
            Result.success(Unit)
        } else {
            Result.failure(Exceptions.UriOperationDeniedException("delete"))
        }
    }
}

/**
 * An [ActivityResultContract] to request the modification of a shared [FileResource]
 *
 * @return a successful [Result] without value if the [FileResource] has been deleted. A failed
 * [Result] means the system couldn't deleted the file or the user denied the request (from API 30+)
 */
class ModifyFileResourceRequest : ActivityResultContract<FileResource, Result<Unit>>() {

    override fun createIntent(context: Context, input: FileResource): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST)
                .putExtra(
                    StartIntentSenderForResult.EXTRA_INTENT_SENDER_REQUEST,
                    IntentSenderRequest.Builder(
                        MediaStore.createDeleteRequest(
                            context.contentResolver,
                            listOf(input.uri)
                        ).intentSender
                    ).build()
                )
        } else {
            Intent()
        }
    }

    override fun getSynchronousResult(context: Context, input: FileResource):
        SynchronousResult<Result<Unit>>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            null
        } else {
            SynchronousResult(Result.success(Unit))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result<Unit> {
        return if (resultCode == Activity.RESULT_OK) {
            Result.success(Unit)
        } else {
            Result.failure(Exceptions.UriOperationDeniedException("modify"))
        }
    }
}
