package com.fmcg.app.data.repository

import com.fmcg.app.data.remote.api.ReportApi
import com.fmcg.app.domain.model.DeliverySummary
import com.fmcg.app.domain.model.DistanceRow
import com.fmcg.app.domain.model.ReportsBundle
import com.fmcg.app.domain.model.SalesRow
import com.fmcg.app.domain.repository.ReportRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val api: ReportApi,
    private val io: CoroutineDispatcher,
) : ReportRepository {

    override suspend fun loadDashboard(): Resource<ReportsBundle> = withContext(io) {
        try {
            coroutineScope {
                val reps = async { api.salesByRep() }
                val products = async { api.salesByProduct() }
                val stores = async { api.salesByStore() }
                val summary = async { api.deliverySummary() }
                val distance = async { api.distanceByRep() }
                val newStores = async { api.newStores() }

                Resource.Success(
                    ReportsBundle(
                        salesByRep = reps.await().map {
                            SalesRow(it.fullName, "${it.orderCount} order(s)", it.totalSales)
                        },
                        salesByProduct = products.await().map {
                            SalesRow(it.name, "${it.quantity} unit(s)", it.total)
                        },
                        salesByStore = stores.await().map {
                            SalesRow(it.name, "${it.orderCount} order(s)", it.total)
                        },
                        deliverySummary = summary.await().let {
                            DeliverySummary(it.pending, it.outForDelivery, it.delivered, it.failed)
                        },
                        distanceByRep = distance.await().map {
                            DistanceRow(it.fullName, it.distanceKm)
                        },
                        newStoresCount = newStores.await().count,
                    )
                )
            }
        } catch (e: HttpException) {
            val msg = if (e.code() == 403) "Reports are admin-only"
                      else "Could not load reports (${e.code()})"
            Resource.Error(msg, e.code())
        } catch (e: IOException) {
            Resource.Error("Offline — reports unavailable")
        }
    }
}
