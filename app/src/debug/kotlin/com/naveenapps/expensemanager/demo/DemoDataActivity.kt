package com.naveenapps.expensemanager.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.naveenapps.expensemanager.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * DEBUG BUILDS ONLY — fills the app with sample data for Play Store screenshots, then opens it.
 *
 * In the app: Settings → Developer → Load demo data. To script it from a computer:
 *   adb shell am start -n com.naveenapps.expensemanager.debug/com.naveenapps.expensemanager.demo.DemoDataActivity \
 *       --es currency INR --es lang hi --ei months 3
 * All extras are optional: currency defaults to the one chosen in the app, lang to the app's
 * language, months to 3. Existing data in the debug app is replaced.
 */
class DemoDataActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currency = intent.getStringExtra(EXTRA_CURRENCY)
        val language = intent.getStringExtra(EXTRA_LANGUAGE)
        val months = intent.getIntExtra(EXTRA_MONTHS, 3).coerceIn(1, 12)

        Toast.makeText(this, "Loading demo data…", Toast.LENGTH_SHORT).show()
        scope.launch {
            try {
                val result = DemoDataSeeder().seed(currency, language, months)
                Log.i(TAG, "Demo data loaded: $result")
                Toast.makeText(
                    applicationContext,
                    "Demo data: ${result.transactions} transactions in ${result.currencyCode}",
                    Toast.LENGTH_SHORT,
                ).show()
                // Explicit MainActivity: in debug builds this activity is also a launcher entry,
                // so getLaunchIntentForPackage() could point back here.
                startActivity(
                    Intent(this@DemoDataActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Demo data failed", e)
                Toast.makeText(applicationContext, "Demo data failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                finish()
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "DemoData"
        const val EXTRA_CURRENCY = "currency"
        const val EXTRA_LANGUAGE = "lang"
        const val EXTRA_MONTHS = "months"
    }
}
