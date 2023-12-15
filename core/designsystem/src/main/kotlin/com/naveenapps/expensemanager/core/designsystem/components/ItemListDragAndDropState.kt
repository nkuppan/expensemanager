package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Modifier.dragGestureHandler(
    scope: CoroutineScope,
    itemListDragAndDropState: ItemListDragAndDropState,
    overscrollJob: MutableState<Job?>,
): Modifier = this.pointerInput(Unit) {
    detectDragGesturesAfterLongPress(
        onDrag = { change, offset ->
            change.consume()
            itemListDragAndDropState.onDrag(offset)
            handleOverscrollJob(overscrollJob, scope, itemListDragAndDropState)
        },
        onDragStart = { offset -> itemListDragAndDropState.onDragStart(offset) },
        onDragEnd = { itemListDragAndDropState.onDragInterrupted() },
        onDragCancel = { itemListDragAndDropState.onDragInterrupted() },
    )
}

private fun handleOverscrollJob(
    overscrollJob: MutableState<Job?>,
    scope: CoroutineScope,
    itemListDragAndDropState: ItemListDragAndDropState,
) {
    if (overscrollJob.value?.isActive == true) return
    val overscrollOffset = itemListDragAndDropState.checkForOverScroll()
    if (overscrollOffset != 0f) {
        overscrollJob.value = scope.launch {
            itemListDragAndDropState.getLazyListState().scrollBy(overscrollOffset)
        }
    } else {
        overscrollJob.value?.cancel()
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit,
): ItemListDragAndDropState {
    return remember { ItemListDragAndDropState(lazyListState, onMove) }
}

class ItemListDragAndDropState(
    private val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit,
) {
    private var draggedDistance by mutableFloatStateOf(0f)
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)
    private var currentIndexOfDraggedItem by mutableIntStateOf(-1)
    private var overscrollJob by mutableStateOf<Job?>(null)

    // Retrieve the currently dragged element's info
    private val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem.let {
            lazyListState.getVisibleItemInfoFor(absoluteIndex = it)
        }

    // Calculate the initial offsets of the dragged element
    private val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { Pair(it.offset, it.offsetEnd) }

    // Calculate the displacement of the dragged element
    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem
            .let { lazyListState.getVisibleItemInfoFor(absoluteIndex = it) }
            ?.let { item ->
                (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }

    // Functions for handling drag gestures
    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
            }
    }

    // Handle interrupted drag gesture
    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = -1
        initiallyDraggedElement = null
        overscrollJob?.cancel()
    }

    // Helper function to calculate start and end offsets
    // Calculate the start and end offsets of the dragged element
    private fun calculateOffsets(offset: Float): Pair<Float, Float> {
        val startOffset = offset + draggedDistance
        val currentElementSize = currentElement?.size ?: 0
        val endOffset = offset + draggedDistance + currentElementSize
        return startOffset to endOffset
    }

    // Handle the drag gesture
    fun onDrag(offset: Offset) {
        draggedDistance += offset.y
        val topOffset = initialOffsets?.first ?: return
        val (startOffset, endOffset) = calculateOffsets(topOffset.toFloat())

        val hoveredElement = currentElement
        if (hoveredElement != null) {
            val delta = startOffset - hoveredElement.offset
            val isDeltaPositive = delta > 0
            val isEndOffsetGreater = endOffset > hoveredElement.offsetEnd

            val validItems = lazyListState.layoutInfo.visibleItemsInfo.filter { item ->
                !(item.offsetEnd < startOffset || item.offset > endOffset || hoveredElement.index == item.index)
            }

            val targetItem = validItems.firstOrNull {
                when {
                    isDeltaPositive -> isEndOffsetGreater
                    else -> startOffset < it.offset
                }
            }

            if (targetItem != null) {
                currentIndexOfDraggedItem.let { current ->
                    onMove.invoke(current, targetItem.index)
                    currentIndexOfDraggedItem = targetItem.index
                }
            }
        }
    }

    fun checkForOverScroll(): Float {
        val draggedElement = initiallyDraggedElement
        if (draggedElement != null) {
            val (startOffset, endOffset) = calculateOffsets(draggedElement.offset.toFloat())
            val diffToEnd = endOffset - lazyListState.layoutInfo.viewportEndOffset
            val diffToStart = startOffset - lazyListState.layoutInfo.viewportStartOffset
            return when {
                draggedDistance > 0 && diffToEnd > 0 -> diffToEnd
                draggedDistance < 0 && diffToStart < 0 -> diffToStart
                else -> 0f
            }
        }
        return 0f
    }

    fun getLazyListState(): LazyListState {
        return lazyListState
    }

    fun getCurrentIndexOfDraggedListItem(): Int {
        return currentIndexOfDraggedItem
    }
}

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this.layoutInfo.visibleItemsInfo.getOrNull(
        absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index,
    )
}

/*
  Bottom offset of the element in Vertical list
*/
val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

fun <T> List<T>.swap(index1: Int, index2: Int): List<T> {
    if (index1 == index2 || index1 < 0 || index2 < 0 || index1 >= size || index2 >= size) {
        return this
    }

    val result = toMutableList()
    val temp = result[index1]
    result[index1] = result[index2]
    result[index2] = temp
    return result
}
