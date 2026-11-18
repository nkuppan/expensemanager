package com.naveenapps.expensemanager.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UQualifiedReferenceExpression

/**
 * Keeps feature modules on the design system: no Material components, text styles, colours or
 * shapes written directly. Applied only to feature modules and :app (core:designsystem is where
 * those are allowed), so it needs no module check of its own.
 */
class DesignSystemDetector :
    Detector(),
    SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        UImportStatement::class.java,
        UCallExpression::class.java,
        UQualifiedReferenceExpression::class.java,
    )

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitImportStatement(node: UImportStatement) {
            val path = node.importReference?.asSourceString() ?: return
            val issue = importIssue(path) ?: return
            context.report(issue, node, context.getLocation(node), importMessage(issue, path))
        }

        override fun visitCallExpression(node: UCallExpression) {
            val name = node.methodIdentifier?.name ?: node.methodName
            if (name != "Color") return
            val argument = node.valueArguments.singleOrNull() as? ULiteralExpression ?: return
            if (argument.value !is Number) return
            context.report(
                HARDCODED_COLOR,
                node,
                context.getLocation(node),
                "Hex colour in a feature. Use a colour from `EmTheme.colors`.",
            )
        }

        override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) {
            if (node.receiver.asSourceString() != "Color") return
            val selector = node.selector.asSourceString()
            if (selector !in FIXED_COLORS) return
            context.report(
                HARDCODED_COLOR,
                node,
                context.getLocation(node),
                "`Color.$selector` ignores light/dark theme. Use a colour from `EmTheme.colors`.",
            )
        }
    }

    companion object {
        private val IMPLEMENTATION = Implementation(DesignSystemDetector::class.java, Scope.JAVA_FILE_SCOPE)

        val RAW_MATERIAL_USAGE: Issue = Issue.create(
            id = "RawMaterialUsage",
            briefDescription = "Material component used outside the design system",
            explanation = """
                Features build UI from the `Em*` components in `core:designsystem`, which apply the \
                app's tokens. Using Material 3 (or the naveenapps library) directly brings back \
                one-off styles. Allowed in features: `androidx.compose.foundation.layout`, `Modifier`, \
                state APIs and `Icons`. If a component is missing, add it to `core:designsystem`.
                """,
            category = Category.USABILITY,
            priority = 6,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )

        val HARDCODED_TEXT_STYLE: Issue = Issue.create(
            id = "HardcodedTextStyle",
            briefDescription = "Font size or weight set in a feature",
            explanation = """
                Text uses a typography role from `EmTheme.type` (screenTitle, sectionTitle, itemTitle, \
                body, supporting, label, overline, amount*). Setting `fontWeight` or `n.sp` in a \
                feature creates another text style.
                """,
            category = Category.USABILITY,
            priority = 6,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )

        val HARDCODED_COLOR: Issue = Issue.create(
            id = "HardcodedColor",
            briefDescription = "Colour literal or colour resource in a feature",
            explanation = """
                Colours come from `EmTheme.colors` so they follow light/dark theme and keep one \
                meaning (income, expense, debt, budget status). Hex literals, `Color.White/Black` \
                and `colorResource` bypass that.
                """,
            category = Category.USABILITY,
            priority = 6,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )

        val HARDCODED_SHAPE: Issue = Issue.create(
            id = "HardcodedShape",
            briefDescription = "Corner shape created in a feature",
            explanation = """
                Corner radii come from `EmTheme.shapes` (xs 4, sm 8, md 12, lg 16, xl 28, pill). \
                Creating `RoundedCornerShape(n.dp)` in a feature adds another radius.
                """,
            category = Category.USABILITY,
            priority = 6,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )

        val ISSUES: List<Issue> = listOf(RAW_MATERIAL_USAGE, HARDCODED_TEXT_STYLE, HARDCODED_COLOR, HARDCODED_SHAPE)

        private val FIXED_COLORS = setOf("White", "Black")

        /** Material 3 APIs a feature may still import (annotations only, no visuals). */
        private val MATERIAL_ALLOWED = setOf(
            "androidx.compose.material3.ExperimentalMaterial3Api",
        )

        private val TEXT_STYLE_IMPORTS = setOf(
            "androidx.compose.ui.text.font.FontWeight",
            "androidx.compose.ui.unit.sp",
        )

        private val COLOR_IMPORTS = setOf(
            "androidx.compose.ui.res.colorResource",
        )

        private val SHAPE_IMPORTS = setOf(
            "androidx.compose.foundation.shape.RoundedCornerShape",
            "androidx.compose.foundation.shape.CircleShape",
            "androidx.compose.foundation.shape.CutCornerShape",
        )

        internal fun importIssue(path: String): Issue? = when {
            path in MATERIAL_ALLOWED -> null
            path == "androidx.compose.material3" || path.startsWith("androidx.compose.material3.") -> RAW_MATERIAL_USAGE
            path.startsWith("androidx.compose.material.") && !path.startsWith("androidx.compose.material.icons") ->
                RAW_MATERIAL_USAGE
            path.startsWith("com.naveenapps.designsystem.") -> RAW_MATERIAL_USAGE
            path in TEXT_STYLE_IMPORTS -> HARDCODED_TEXT_STYLE
            path in COLOR_IMPORTS -> HARDCODED_COLOR
            path in SHAPE_IMPORTS -> HARDCODED_SHAPE
            else -> null
        }

        private fun importMessage(issue: Issue, path: String): String = when (issue) {
            RAW_MATERIAL_USAGE -> "`${path.substringAfterLast('.')}` used directly. Use the `Em*` component " +
                "from `core:designsystem`."
            HARDCODED_TEXT_STYLE -> "Text style set by hand. Use a role from `EmTheme.type`."
            HARDCODED_COLOR -> "Colour resource in a feature. Use a colour from `EmTheme.colors`."
            else -> "Shape created by hand. Use a shape from `EmTheme.shapes`."
        }
    }
}
