package com.fmcg.app.presentation.navigation

import com.fmcg.app.domain.model.UserRole

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val ADMIN_HOME = "admin_home"
    const val MARKETING_HOME = "marketing_home"
    const val DELIVERY_HOME = "delivery_home"
    const val MARKETING_ROUTE_MAP = "marketing_route_map"
    const val LOCATION_PICKER = "location_picker"
    const val STORE_LIST = "store_list"
    const val STORE_DETAIL = "store_detail/{storeId}"
    const val STORE_FORM = "store_form?storeId={storeId}"
    const val ORDER_LIST = "order_list?storeId={storeId}"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val ORDER_CREATE = "order_create?storeId={storeId}"
    const val DELIVERY_DETAIL = "delivery_detail/{deliveryId}"
    const val CAMERA_CAPTURE = "camera_capture"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_USERS = "admin_users"

    fun storeDetail(id: Int) = "store_detail/$id"
    fun storeForm(id: Int? = null) = "store_form?storeId=${id ?: -1}"
    fun orderList(storeId: Int? = null) = "order_list?storeId=${storeId ?: -1}"
    fun orderDetail(id: Int) = "order_detail/$id"
    fun orderCreate(storeId: Int? = null) = "order_create?storeId=${storeId ?: -1}"
    fun deliveryDetail(id: Int) = "delivery_detail/$id"

    fun homeFor(role: UserRole): String = when (role) {
        UserRole.ADMIN -> ADMIN_HOME
        UserRole.MARKETING -> MARKETING_HOME
        UserRole.DELIVERY -> DELIVERY_HOME
        UserRole.UNKNOWN -> LOGIN
    }
}
