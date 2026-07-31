package com.fmcg.app.data.repository

import com.fmcg.app.data.mapper.toDomain
import com.fmcg.app.data.remote.api.DeliveryApi
import com.fmcg.app.data.remote.api.MediaApi
import com.fmcg.app.data.remote.dto.DeliveryStatusUpdateDto
import com.fmcg.app.domain.model.Delivery
import com.fmcg.app.domain.model.DeliveryStatus
import com.fmcg.app.domain.repository.DeliveryRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

class DeliveryRepositoryImpl @Inject constructor(
    private val api: DeliveryApi,
    private val mediaApi: MediaApi,
    private val io: CoroutineDispatcher,
) : DeliveryRepository {

    override suspend fun listMine(): Resource<List<Delivery>> = withContext(io) {
        try {
            // Backend scopes delivery staff to their own assignments automatically.
            Resource.Success(api.list().items.map { it.toDomain() })
        } catch (e: HttpException) {
            Resource.Error("Could not load deliveries (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — deliveries unavailable")
        }
    }

    override suspend fun getDelivery(id: Int): Resource<Delivery> = withContext(io) {
        try {
            Resource.Success(api.get(id).toDomain())
        } catch (e: HttpException) {
            Resource.Error("Delivery not found (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — delivery unavailable")
        }
    }

    override suspend fun updateStatus(
        id: Int, status: DeliveryStatus, notes: String?, proofPhotoUrl: String?,
    ): Resource<Delivery> = withContext(io) {
        try {
            val body = DeliveryStatusUpdateDto(status.api, notes, proofPhotoUrl)
            Resource.Success(api.updateStatus(id, body).toDomain())
        } catch (e: HttpException) {
            val msg = if (e.code() == 403) "You can only update your own deliveries"
                      else "Could not update delivery (${e.code()})"
            Resource.Error(msg, e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — try again when connected")
        }
    }

    override suspend fun uploadProof(file: File): Resource<String> = withContext(io) {
        try {
            val part = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody("image/jpeg".toMediaType())
            )
            Resource.Success(mediaApi.upload(part).url)
        } catch (e: HttpException) {
            Resource.Error("Upload failed (${e.code()})", e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — could not upload photo")
        }
    }
}
