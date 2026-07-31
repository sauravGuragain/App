package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.Delivery
import com.fmcg.app.domain.model.DeliveryStatus
import com.fmcg.app.util.Resource
import java.io.File

interface DeliveryRepository {
    suspend fun listMine(): Resource<List<Delivery>>
    suspend fun getDelivery(id: Int): Resource<Delivery>
    suspend fun updateStatus(
        id: Int,
        status: DeliveryStatus,
        notes: String? = null,
        proofPhotoUrl: String? = null,
    ): Resource<Delivery>
    /** Upload a proof image; returns the stored URL. */
    suspend fun uploadProof(file: File): Resource<String>
}
