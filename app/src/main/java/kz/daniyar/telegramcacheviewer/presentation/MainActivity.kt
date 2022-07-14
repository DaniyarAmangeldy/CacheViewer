package kz.daniyar.telegramcacheviewer.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.properties.Delegates
import kz.daniyar.telegramcacheviewer.R
import kz.daniyar.telegramcacheviewer.utils.EnvironmentCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var storagePermissionRequestLauncher: ActivityResultLauncher<String> by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainActivityContent()
        }
        storagePermissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // skip handle here, will check permission onResume
        }
    }

    @Composable
    private fun MainActivityContent() {
        val cacheList by viewModel.cacheList.collectAsState()
        val lifecycleOwner = LocalLifecycleOwner.current
        var hasManageStoragePermission by remember { mutableStateOf(hasReadStoragePermission()) }
        if (hasManageStoragePermission) {
            CacheList(cacheList)
        } else {
            RequestPermissionContent(::onRequestPermissionClick)
        }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    hasManageStoragePermission = hasReadStoragePermission()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(hasManageStoragePermission) {
            if (hasManageStoragePermission) {
                viewModel.onLoadCacheRequest()
            }
        }
    }

    @Composable
    private fun CacheList(cacheList: List<CacheDVO>) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cacheList) { item ->
                CacheItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = item
                )
            }
        }
    }

    @Composable
    private fun RequestPermissionContent(onRequestClick: () -> Unit) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.message_give_access),
                    style = MaterialTheme.typography.h5
                )
                Button(onClick = onRequestClick) {
                    Text(
                        text = stringResource(R.string.open_permission),
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }
    }

    @Composable
    private fun CacheItem(
        item: CacheDVO,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = item.extension,
                style = MaterialTheme.typography.body2
            )
            Text(
                text = item.size,
                style = MaterialTheme.typography.caption
            )
        }
    }

    private fun hasReadStoragePermission() = EnvironmentCompat.checkReadExternalStoragePermission(this)

    private fun onRequestPermissionClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tryNavigateToSettingsPermission()
            return
        }
        if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            openAppSettings()
            return
        }
        storagePermissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun tryNavigateToSettingsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }
        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }
}