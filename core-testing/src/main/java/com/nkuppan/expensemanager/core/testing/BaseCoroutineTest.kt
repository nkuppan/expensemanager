package com.nkuppan.expensemanager.core.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
open class BaseCoroutineTest {

    @get:Rule
    val testCoroutineDispatcher = TestDispatcherRule()

    private lateinit var autoCloseable: AutoCloseable

    @Before
    open fun onCreate() {
        autoCloseable = MockitoAnnotations.openMocks(this)
    }

    @After
    open fun onDestroy() {
        autoCloseable.close()
    }
}