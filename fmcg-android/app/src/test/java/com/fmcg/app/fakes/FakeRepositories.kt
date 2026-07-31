package com.fmcg.app.fakes

import com.fmcg.app.domain.model.Order
import com.fmcg.app.domain.model.OrderDraft
import com.fmcg.app.domain.model.OrderStatus
import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.model.Store
import com.fmcg.app.domain.model.StoreDraft
import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.repository.AuthRepository
import com.fmcg.app.domain.repository.OrderRepository
import com.fmcg.app.domain.repository.ProductRepository
import com.fmcg.app.domain.repository.PushRepository
import com.fmcg.app.domain.repository.StoreRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository(
    private val loginResult: Resource<User>,
    private val loggedIn: Boolean = true,
) : AuthRepository {
    override suspend fun login(email: String, password: String) = loginResult
    override suspend fun currentUser() = loginResult
    override fun isLoggedIn() = loggedIn
    override fun logout() {}
}

class FakePushRepository : PushRepository {
    override suspend fun register(token: String) {}
    override suspend fun registerCurrentToken() {}
}

class FakeProductRepository(private val products: List<Product> = emptyList()) : ProductRepository {
    override fun observeProducts(): Flow<List<Product>> = flowOf(products)
    override suspend fun refresh() = Resource.Success(Unit)
}

class FakeStoreRepository(private val stores: List<Store> = emptyList()) : StoreRepository {
    override fun observeStores(query: String): Flow<List<Store>> = flowOf(stores)
    override suspend fun refresh() = Resource.Success(Unit)
    override suspend fun getStore(id: Int): Resource<Store> =
        stores.firstOrNull { it.id == id }?.let { Resource.Success(it) }
            ?: Resource.Error("not found")
    override suspend fun createStore(draft: StoreDraft) =
        Resource.Success(Store(1, draft.name, draft.ownerName, draft.phone, draft.address,
            draft.latitude, draft.longitude, draft.notes, null))
    override suspend fun updateStore(id: Int, draft: StoreDraft) =
        Resource.Success(Store(id, draft.name, draft.ownerName, draft.phone, draft.address,
            draft.latitude, draft.longitude, draft.notes, null))
    override suspend fun deleteStore(id: Int) = Resource.Success(Unit)
}

class FakeOrderRepository(
    private val createResult: Resource<Boolean> = Resource.Success(false),
) : OrderRepository {
    var createdDraft: OrderDraft? = null
    override suspend fun listOrders(storeId: Int?) = Resource.Success(emptyList<Order>())
    override suspend fun getOrder(id: Int): Resource<Order> = Resource.Error("not found")
    override suspend fun createOrder(draft: OrderDraft): Resource<Boolean> {
        createdDraft = draft
        return createResult
    }
    override suspend fun updateStatus(id: Int, status: OrderStatus): Resource<Order> =
        Resource.Error("not found")
}
