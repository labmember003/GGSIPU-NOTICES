package com.falcon.ggsipunotices

import android.content.Context
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.falcon.ggsipunotices.model.Notice
import com.falcon.ggsipunotices.ui.NoticeItem
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperNoticeItem(
    notice: Notice,
    modifier: Modifier = Modifier,
    startDownloading: (String, Context, String?, Int, CoroutineScope, ComponentActivity?) -> Unit,
    openFile: (Context, File) -> Unit,
    shareFile: (String, File, Boolean) -> Unit,
    activity: ComponentActivity?,
    newNotices: List<Notice>
) {
    val context = LocalContext.current
    val currentItem by rememberUpdatedState(notice)
    val fileTitle = removeNonAlphaNumeric(currentItem.title.toString()).plus(".pdf") // For download / share purposes
    val file = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
        fileTitle
    )
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when(it) {
                StartToEnd -> {
                    // CurrentItem // Share
                    // StartToEnd Swiped
                    if (file.exists()) {
                        shareFile(fileTitle, file, true)
                    } else {
//                        shareFile(fileTitle, file, false)
                        startDownloading(fileTitle, context, notice.url, Random().nextInt(), scope, activity)
                    }
                }
                EndToStart -> {
                    // CurrentItem // Download
                    // EndToStart Swiped
                    if (file.exists()) {
                        openFile(context, file)
                    } else {
                        startDownloading(fileTitle, context, notice.url, Random().nextInt(), scope, activity)
                    }
                }
                Settled -> return@rememberSwipeToDismissBoxState false
            }
            return@rememberSwipeToDismissBoxState false
        },
        // positional threshold of 25%
        positionalThreshold = { it * .25f }
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { DismissBackground(dismissState)},
        content = {
            NoticeItem(
                notice = notice,
                newNotices = newNotices
            )
        })
}

fun removeNonAlphaNumeric(input: String): String {
    val regex = "[^a-zA-Z0-9]".toRegex()
    return input.replace(regex, "")
}
