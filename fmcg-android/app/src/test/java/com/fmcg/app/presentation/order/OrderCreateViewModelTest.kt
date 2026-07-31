package com.fmcg.app.presentation.order

import androidx.lifecycle.SavedStateHandle
import com.fmcg.app.MainDispatcherRule
import com.fmcg.app.domain.model.Product
import com.fmcg.app.domain.model.Store
import com.fmcg.app.fakes.FakeOrderRepository
import com.fmcg.app.fakes.FakeProductRepository
import com.fmcg.app.fakes.FakeStoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderCreateViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val product = Product(10, "Cola", "COLA", "pcs", "1.20", true)
    private val store = Store(5, "Mart", null, null, null, 27.7, 85.3, null, null)

    private fun viewModel(order: FakeOrderRepository = FakeOrderRepository()) =
        OrderCreateViewModel(
            order,
            FakeProductRepository(listOf(product)),
            FakeStoreRepository(listOf(store)),
            SavedStateHandle(),
        )

    @Test
    fun line_total_is_computed_from_price_qty_discount() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        val key = vm.state.value.lines.first().key
        vm.selectProduct(key, product)
        vm.updateLine(key, quantity = "2", unitPrice = "3.00", discount = "1.00")
        assertEquals("5.00", vm.state.value.total)   // 2*3.00 - 1.00
    }

    @Test
    fun cannot_save_without_store_or_product() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(!vm.state.value.canSave)
    }

    @Test
    fun submit_sends_draft_and_marks_submitted() = runTest {
        val fakeOrders = FakeOrderRepository()
        val vm = viewModel(fakeOrders)
        advanceUntilIdle()
        val key = vm.state.value.lines.first().key
        vm.setStore(store.id)
        vm.selectProduct(key, product)
        vm.updateLine(key, quantity = "1", unitPrice = "1.20")
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.state.value.submitted)
        assertNotNull(fakeOrders.createdDraft)
        assertEquals(store.id, fakeOrders.createdDraft!!.storeId)
    }
}
