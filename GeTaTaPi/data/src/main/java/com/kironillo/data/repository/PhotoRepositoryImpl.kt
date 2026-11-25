package com.kironillo.data.repository

import android.content.Context
import android.net.Uri
import com.kironillo.data.local.datastore.AuthSessionDataStore
import com.kironillo.data.remote.StorageApi
import com.kironillo.data.remote.SubmissionApi
import com.kironillo.data.remote.dto.CreateSubmissionRequestDto
import com.kironillo.data.mapper.toDomain
import com.kironillo.domain.model.Answer
import com.kironillo.domain.model.SubmissionHistoryItem
import com.kironillo.domain.model.SubmissionHistoryPage
import com.kironillo.domain.model.SubmissionResult
import com.kironillo.domain.repository.PhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Named

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authSessionDataStore: AuthSessionDataStore,
    private val storageApi: StorageApi,
    private val submissionApi: SubmissionApi,
    @param:Named("supabaseUrl") private val supabaseUrl: String,
) : PhotoRepository {

    override suspend fun submitPhoto(
        taskId: String,
        photoPath: String,
        idempotencyKey: String,
    ): String {
        val userId = authSessionDataStore.getUserId()
            ?: throw IllegalStateException("Пользователь не авторизован")

        val uri = Uri.parse(photoPath)

        val mimeType = context.contentResolver.getType(uri)
            ?: when (uri.lastPathSegment?.substringAfterLast('.', "")) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }

        require(mimeType in SUPPORTED_MIME_TYPES) {
            "Формат фотографии не поддерживается"
        }

        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val photoBytes = context.contentResolver
            .openInputStream(uri)
            ?.use { it.readBytes() }
            ?: throw IllegalStateException("Не удалось прочитать фотографию")

        require(photoBytes.isNotEmpty()) {
            "Фотография пустая"
        }

        require(photoBytes.size <= MAX_PHOTO_SIZE) {
            "Размер фотографии превышает 10 МБ"
        }

        val storagePath = "$userId/$taskId/$idempotencyKey.$extension"

        val uploadResponse = storageApi.uploadPhoto(
            path = storagePath,
            contentType = mimeType,
            body = photoBytes.toRequestBody(mimeType.toMediaType()),
        )

        if (!uploadResponse.isSuccessful) {
            val error = uploadResponse.errorBody()?.string()
            throw IllegalStateException(
                "Не удалось загрузить фотографию: ${error ?: uploadResponse.code()}",
            )
        }

        val submission = submissionApi.createSubmission(
            idempotencyKey = idempotencyKey,
            request = CreateSubmissionRequestDto(
                taskId = taskId,
                storagePath = storagePath,
            ),
        )

        return submission.id
    }

    override suspend fun getSubmissionStatus(
        submissionId: String,
    ): SubmissionResult {
        val response = submissionApi.getSubmission(submissionId)

        return response.toDomain()
    }

    override suspend fun getPhotoUrl(storagePath: String): String {
        val response = storageApi.createSignedUrl(
            path = storagePath,
        )

        val signedPath = response.signedURL

        return if (signedPath.startsWith("http")) {
            signedPath
        } else {
            supabaseUrl.trimEnd('/') +
                "/storage/v1" +
                (if (signedPath.startsWith("/")) signedPath else "/$signedPath")
        }
    }

    override suspend fun getSubmissionHistory(
        limit: Int,
        cursor: String?,
    ): SubmissionHistoryPage {
        val response = submissionApi.getSubmissions(
            limit = limit,
            cursor = cursor,
        )

        return SubmissionHistoryPage(
            items = response.data.map { it.toDomain() },
            nextCursor = response.page.nextCursor,
        )
    }

    override suspend fun checkPhoto(
        topic: String,
        photoPath: String,
    ): Answer {
        throw UnsupportedOperationException(
            "Проверка фотографии выполняется через отправку задания",
        )
    }

    override suspend fun savePhoto(
        photoPath: String,
        latitude: Double?,
        longitude: Double?,
    ) {
        throw UnsupportedOperationException(
            "Для отправки фотографии используйте submitPhoto",
        )
    }

    private companion object {
        const val MAX_PHOTO_SIZE = 10 * 1024 * 1024

        val SUPPORTED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/webp",
        )
    }
}
