package com.naveenapps.expensemanager.common.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule

@ExperimentalCoroutinesApi
open class BaseCoroutineTest {

    @get:Rule
    val testCoroutineDispatcher = TestDispatcherRule()

    @Before
    open fun onCreate() {

    }

    @After
    open fun onDestroy() {

    }
}