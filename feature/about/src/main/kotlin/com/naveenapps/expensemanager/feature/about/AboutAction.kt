package com.naveenapps.expensemanager.feature.about

sealed class AboutAction {

    data object ClosePage : AboutAction()

    data object Github : AboutAction()

    data object Twitter : AboutAction()

    data object Instagram : AboutAction()

    data object Mail : AboutAction()

    data object OpenAboutUsPage : AboutAction()

    data object OpenTerms : AboutAction()

    data object OpenPrivacy : AboutAction()

    data object OpenLicense : AboutAction()
}