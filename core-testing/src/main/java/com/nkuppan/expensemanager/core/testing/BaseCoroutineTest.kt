package com.nkuppan.expensemanager.core.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule

@ExperimentalCoroutinesApi
open class BaseCoroutineTest {

    @get:Rule
    val testCoroutineDispatcher = TestDispatcherRule()

    private lateinit var autoCloseable: AutoCloseable

    @Before
    open fun onCreate() {

    }

    @After
    open fun onDestroy() {
        autoCloseable.close()
    }
}