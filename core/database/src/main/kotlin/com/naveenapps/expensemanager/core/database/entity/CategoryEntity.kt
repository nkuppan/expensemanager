package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.naveenapps.expensemanager.core.model.CategoryType
import kotlin.time.Instant

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: CategoryType,
    @ColumnInfo(name = "icon_background_color")
    val iconBackgroundColor: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Instant,
    @ColumnInfo(name = "created_on")
    val createdOn: Instant,
    @ColumnInfo(name = "default_category_key", defaultValue = "NULL")
    val defaultCategoryKey: String? = null,
    @ColumnInfo(name = "custom_image_path", defaultValue = "NULL")
    val customImagePath: String? = null,
)
