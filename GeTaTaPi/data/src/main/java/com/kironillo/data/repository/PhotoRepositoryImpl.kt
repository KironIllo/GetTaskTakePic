package com.kironillo.data.repository

import android.content.Context
import android.net.Uri
import com.kironillo.data.local.datastore.AuthSessionDataStore
import com.kironillo.data.mapper.toDomain
import com.kironillo.data.remote.NetworkErrorHandler
import com.kironillo.data.remote.StorageApi
import com.kironillo.data.remote.SubmissionApi
import com.kironillo.data.remote.dto.CreateSubmissionRequestDto
import com.kironillo.domain.model.Answer
import com.kironillo.domain.model.SubmissionHistoryPage
import com.kironillo.domain.model.SubmissionResult
import com.kironillo.domain.repository.PhotoRepository
import com.kironillo.domain.result.DomainException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authSessionDataStore: AuthSessionDataStore,
    private val storageApi: StorageApi,
    private val submissionApi: SubmissionApi,
    private val errorHandler: NetworkErrorHandler,
    @param:Named("supabaseUrl") private val supabaseUrl: String,
) : PhotoRepository {

    override suspend fun submitPhoto(
        taskId: String,
        photoPath: String,
        idempotencyKey: String,
        latitude: Double?,
        longitude: Double?,
    ): String = mapNetworkErrors {
        submitPhotoInternal(taskId, photoPath, idempotencyKey, latitude, longitude)
    }

    private suspend fun submitPhotoInternal(
        taskId: String,
        photoPath: String,
        idempotencyKey: String,
        latitude: Double?,
        longitude: Double?,
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
            throw HttpException(uploadResponse)
        }

        val submission = submissionApi.createSubmission(
            idempotencyKey = idempotencyKey,
            request = CreateSubmissionRequestDto(
                taskId = taskId,
                storagePath = storagePath,
                latitude = latitude,
                longitude = longitude,
            ),
        )

        return submission.id
    }

    override suspend fun getSubmissionStatus(
        submissionId: String,
    ): SubmissionResult = mapNetworkErrors {
        submissionApi.getSubmission(submissionId).toDomain()
    }

    override suspend fun getPhotoUrl(storagePath: String): String = mapNetworkErrors {
        val response = storageApi.createSignedUrl(
            path = storagePath,
        )

        val signedPath = response.signedURL

        if (signedPath.startsWith("http")) {
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
    ): SubmissionHistoryPage = mapNetworkErrors {
        val response = submissionApi.getSubmissions(
            limit = limit,
            cursor = cursor,
        )

        SubmissionHistoryPage(
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

    private suspend fun <T> mapNetworkErrors(block: suspend () -> T): T =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: DomainException) {
            throw exception
        } catch (exception: HttpException) {
            throw DomainException(errorHandler.mapError(exception))
        } catch (exception: IOException) {
            throw DomainException(errorHandler.mapError(exception))
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
