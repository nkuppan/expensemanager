package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.openEmailOption
import com.naveenapps.expensemanager.core.common.utils.openEmailToOption
import com.naveenapps.expensemanager.core.common.utils.openPrintOption
import com.naveenapps.expensemanager.core.common.utils.openRateUs
import com.naveenapps.expensemanager.core.common.utils.openShareOption
import com.naveenapps.expensemanager.core.common.utils.openWebPage
import com.naveenapps.expensemanager.core.repository.FirebaseSettingsRepository
import com.naveenapps.expensemanager.core.repository.ShareRepository
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.File
import javax.inject.Inject

class ShareRepositoryImpl @Inject constructor(
    @ActivityContext val context: Context,
    private val firebaseSettingsRepository: FirebaseSettingsRepository
) : ShareRepository {

    override fun sendEmail(file: File?) {
        file?.let {
            context.openEmailOption(it)
        } ?: run {
            openEmailToOption(context, firebaseSettingsRepository.getFeedbackEmail())
        }
    }

    override fun share(file: File?) {
        file?.let {
            context.openShareOption(it)
        }
    }

    override fun print(file: File?) {
        file?.let {
            context.openPrintOption(it)
        }
    }

    override fun openRateUs() {
        context.openRateUs()
    }

    override fun openPrivacy() {
        context.openWebPage(firebaseSettingsRepository.getPrivacyURL())
    }

    override fun openTerms() {
        context.openWebPage(firebaseSettingsRepository.getTermsURL())
    }

    override fun openAboutUs() {
        context.openWebPage(firebaseSettingsRepository.getAboutUsURL())
    }

    override fun openGithub() {
        context.openWebPage(firebaseSettingsRepository.getGithubURL())
    }

    override fun openInstagram() {
        context.openWebPage(firebaseSettingsRepository.getInstagramURL())
    }

    override fun openTwitter() {
        context.openWebPage(firebaseSettingsRepository.getTwitterURL())
    }
}