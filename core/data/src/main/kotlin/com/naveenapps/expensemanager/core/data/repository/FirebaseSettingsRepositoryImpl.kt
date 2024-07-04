package com.naveenapps.expensemanager.core.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.naveenapps.expensemanager.core.repository.FirebaseSettingsRepository
import javax.inject.Inject

class FirebaseSettingsRepositoryImpl @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) : FirebaseSettingsRepository {

    override fun getPrivacyURL(): String {
        return firebaseRemoteConfig.getString("privacy_url").ifBlank {
            DEFAULT_PRIVACY_URL
        }
    }

    override fun getTermsURL(): String {
        return firebaseRemoteConfig.getString("terms_url").ifBlank {
            DEFAULT_TERMS_URL
        }
    }

    override fun getAboutUsURL(): String {
        return firebaseRemoteConfig.getString("about_us_url").ifBlank {
            DEFAULT_ABOUT_US_URL
        }
    }

    override fun getGithubURL(): String {
        return firebaseRemoteConfig.getString("github_url").ifBlank {
            DEFAULT_GITHUB_URL
        }
    }

    override fun getInstagramURL(): String {
        return firebaseRemoteConfig.getString("instagram_url").ifBlank {
            DEFAULT_INSTAGRAM_URL
        }
    }

    override fun getTwitterURL(): String {
        return firebaseRemoteConfig.getString("twitter_url").ifBlank {
            DEFAULT_TWITTER_URL
        }
    }

    override fun getFeedbackEmail(): String {
        return firebaseRemoteConfig.getString("feedback_email").ifBlank {
            DEFAULT_FEEDBACK_EMAIL
        }
    }

    override fun getMaxBusinessProfileCount(): Long {
        val count = firebaseRemoteConfig.getLong("max_business_profile_count")
        return if (count > 0) count else DEFAULT_MAX_BUSINESS_PROFILE_COUNT
    }

    override fun getMaxInvoicesCount(): Long {
        val count = firebaseRemoteConfig.getLong("max_invoices_count")
        return if (count > 0) count else DEFAULT_MAX_INVOICES_COUNT
    }

    override fun getMaxClientsCount(): Long {
        val count = firebaseRemoteConfig.getLong("max_clients_count")
        return if (count > 0) count else DEFAULT_MAX_CLIENT_COUNT
    }

    override fun getMaxItemsCount(): Long {
        val count = firebaseRemoteConfig.getLong("max_items_count")
        return if (count > 0) count else DEFAULT_MAX_ITEMS_COUNT
    }

    override fun getMaxPaymentMethodCount(): Long {
        val count = firebaseRemoteConfig.getLong("max_payment_method_count")
        return if (count > 0) count else DEFAULT_MAX_PAYMENT_METHOD_COUNT
    }

    override fun getMaxTermsAndConditionCount(): Long {
        val count = firebaseRemoteConfig.getLong("max_terms_and_condition_count")
        return if (count > 0) count else DEFAULT_MAX_TERMS_AND_CONDITION_COUNT
    }

    companion object {
        private const val DEFAULT_MAX_BUSINESS_PROFILE_COUNT: Long = 1
        private const val DEFAULT_MAX_INVOICES_COUNT: Long = 10
        private const val DEFAULT_MAX_CLIENT_COUNT: Long = 4
        private const val DEFAULT_MAX_ITEMS_COUNT: Long = 10
        private const val DEFAULT_MAX_PAYMENT_METHOD_COUNT: Long = 10
        private const val DEFAULT_MAX_TERMS_AND_CONDITION_COUNT: Long = 10

        private const val DEFAULT_PRIVACY_URL: String =
            "https://expensemanager.naveenapps.com/privacy-policy"
        private const val DEFAULT_TERMS_URL: String =
            "https://expensemanager.naveenapps.com/terms"
        private const val DEFAULT_ABOUT_US_URL: String = "https://expensemanager.naveenapps.com"
        private const val DEFAULT_GITHUB_URL: String = "https://www.github.com/nkuppan"
        private const val DEFAULT_INSTAGRAM_URL: String =
            "https://www.instagram.com/naveenkumar_kup"
        private const val DEFAULT_TWITTER_URL: String = "https://www.twitter.com/naveenkumarn27"
        private const val DEFAULT_FEEDBACK_EMAIL: String = "naveenkumar@naveenapps.com"
    }
}