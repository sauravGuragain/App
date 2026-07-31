package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.ReportsBundle
import com.fmcg.app.util.Resource

interface ReportRepository {
    suspend fun loadDashboard(): Resource<ReportsBundle>
}
