package com.naveenapps.expensemanager.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class DesignSystemIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = DesignSystemDetector.ISSUES + SharedLayerApiDetector.ISSUES

    override val api: Int = CURRENT_API

    override val minApi: Int = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "Expense Manager",
        identifier = "expensemanager-design-system",
    )
}
