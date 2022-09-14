package com.nkuppan.expensemanager.core.testing

import java.io.InputStreamReader

class FakeResponseFileReader(path: String) {

    val content: String

    init {
        val reader = InputStreamReader(this.javaClass.classLoader?.getResourceAsStream(path))
        content = reader.readText()
        reader.close()
    }
}
