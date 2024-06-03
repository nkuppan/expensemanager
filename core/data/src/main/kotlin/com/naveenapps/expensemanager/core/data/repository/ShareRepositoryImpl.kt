package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.openEmailOption
import com.naveenapps.expensemanager.core.common.utils.openPrintOption
import com.naveenapps.expensemanager.core.common.utils.openRateUs
import com.naveenapps.expensemanager.core.common.utils.openShareOption
import com.naveenapps.expensemanager.core.repository.ShareRepository
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.File
import javax.inject.Inject

class ShareRepositoryImpl @Inject constructor(
    @ActivityContext val context: Context
) : ShareRepository {
    override fun sendEmail(file: File?) {
        file?.let {
            context.openEmailOption(it)
        }
    }

    override fun share(file: File?) {
        file?.let {
            context.openShareOption(it)
        }
    }

    override fun openPrinter(file: File?) {
        file?.let {
            context.openPrintOption(it)
        }
    }

    override fun openRateUs() {
        context.openRateUs()
    }
}