package com.nkuppan.expensemanager.presentation.settings.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.nkuppan.expensemanager.data.db.entity.TransactionEntity
import jxl.CellView
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.UnderlineStyle
import jxl.write.*
import jxl.write.biff.RowsExceededException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object ExcelFileExport {

    // Storage Permissions
    const val REQUEST_EXTERNAL_STORAGE = 1

    private var timesBoldUnderline: WritableCellFormat? = null

    private var times: WritableCellFormat? = null

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param aFragment to validate the permissions and getting the results to the same fragment
     */
    internal fun verifyStoragePermissions(aFragment: Fragment): Boolean {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            aFragment.requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            aFragment.requestPermissions(
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )

            return false
        }

        return true
    }

    @Throws(IOException::class, WriteException::class)
    fun write(context: Context, aCategoryValues: List<TransactionEntity>): String {

        val exportPath = FileUtils.getExcelFilePath(context)

        val file = File(exportPath, "${System.currentTimeMillis()}.xls")

        if (!file.exists()) {
            file.createNewFile()
        }

        val wbSettings = WorkbookSettings()

        wbSettings.locale = Locale("en", "EN")

        val workbook = Workbook.createWorkbook(file, wbSettings)
        workbook.createSheet("Report", 0)
        val excelSheet = workbook.getSheet(0)

        createLabel(excelSheet)
        createContent(excelSheet, aCategoryValues)

        workbook.write()
        workbook.close()

        return file.absolutePath
    }

    @Throws(WriteException::class)
    private fun createLabel(sheet: WritableSheet) {
        // Lets create a times font
        val times10pt = WritableFont(WritableFont.TIMES, 10)
        // Define the cell format
        times = WritableCellFormat(times10pt)
        // Lets automatically wrap the cells
        times!!.wrap = true

        // create create a bold font with unterlines
        val times10ptBoldUnderline = WritableFont(
            WritableFont.TIMES,
            10,
            WritableFont.BOLD,
            false,
            UnderlineStyle.SINGLE
        )
        timesBoldUnderline = WritableCellFormat(times10ptBoldUnderline)
        // Lets automatically wrap the cells
        timesBoldUnderline!!.wrap = true

        val cv = CellView()
        cv.format = times
        cv.format = timesBoldUnderline
        cv.isAutosize = true

        // Write a few headers
        addCaption(sheet, 0, 0, "Date")
        addCaption(sheet, 1, 0, "Notes")
        addCaption(sheet, 2, 0, "Category type")
        addCaption(sheet, 3, 0, "Amount")
        addCaption(sheet, 4, 0, "Type")
    }

    @Throws(WriteException::class, RowsExceededException::class)
    private fun createContent(sheet: WritableSheet, aTransactionValue: List<TransactionEntity>) {

        aTransactionValue.forEachIndexed { aIndex, aValue ->
            // First column
            addLabel(
                sheet,
                0,
                (aIndex + 1),
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(aValue.createdOn)
            )
            // Second column
            addLabel(
                sheet,
                1,
                (aIndex + 1),
                aValue.notes
            )
            // Third column
            addLabel(
                sheet,
                2,
                (aIndex + 1),
                aValue.categoryId
            )
            // Fourth column
            addNumber(
                sheet,
                3,
                (aIndex + 1),
                aValue.amount
            )
            // Fourth column
            addLabel(
                sheet,
                4,
                (aIndex + 1),
                "Expense"//if (aValue.category?.type == 1) "Expense" else "Income"
            )
        }
    }

    @Throws(RowsExceededException::class, WriteException::class)
    private fun addCaption(sheet: WritableSheet, column: Int, row: Int, s: String) {
        val label = Label(column, row, s, timesBoldUnderline)
        sheet.addCell(label)
    }

    @Throws(WriteException::class, RowsExceededException::class)
    private fun addNumber(
        aWorkSheet: WritableSheet,
        aColumn: Int,
        aRow: Int,
        integer: Double
    ) {
        val number = Number(aColumn, aRow, integer, times)
        aWorkSheet.addCell(number)
    }

    @Throws(WriteException::class, RowsExceededException::class)
    private fun addLabel(
        aWorkSheet: WritableSheet,
        aColumn: Int,
        aRow: Int,
        aValue: String
    ) {
        val label = Label(aColumn, aRow, aValue, times)
        aWorkSheet.addCell(label)
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }
}