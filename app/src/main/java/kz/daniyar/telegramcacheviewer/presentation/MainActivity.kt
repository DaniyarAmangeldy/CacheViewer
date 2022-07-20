package kz.daniyar.telegramcacheviewer.presentation

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.properties.Delegates
import kz.daniyar.telegramcacheviewer.R
import kz.daniyar.telegramcacheviewer.domain.entity.Cache
import kz.daniyar.telegramcacheviewer.utils.EnvironmentCompat
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var legacyReadExternalStoragePermissionRequestLauncher: ActivityResultLauncher<String> by Delegates.notNull()
    private var requestPermissionDeniedPermanently: Boolean = false
    private var readExternalStoragePermissionRequestLauncher: ActivityResultLauncher<Intent> by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainActivityContent()
        }
        legacyReadExternalStoragePermissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return@registerForActivityResult
            }
            val shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)
            requestPermissionDeniedPermanently = !granted && !shouldShowRequestPermissionRationale
            if (granted) {
                viewModel.onLoadCacheRequest()
            }
        }
        readExternalStoragePermissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val uri = it.data?.data ?: return@registerForActivityResult
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.onLoadCacheRequest(uri)
        }
    }

    @Composable
    private fun MainActivityContent() {
        val uiState by viewModel.uiState.collectAsState()
        val event by viewModel.event.collectAsState(Event.Idle)
        val lifecycleOwner = LocalLifecycleOwner.current

        when (val uiState = uiState) {
            is UiState.CacheList -> CacheList(uiState.cacheList)
            UiState.CacheLoading -> ListLoading()
            UiState.PermissionRequired -> RequestPermissionContent(::onRequestPermissionClick)
            UiState.Idle -> ListLoading()
        }

        LaunchedEffect(event) {
            when (event) {
                Event.CheckPermission -> {
                    if (!hasReadStoragePermission()) {
                        viewModel.onNoReadStoragePermission()
                    } else {
                        viewModel.onLoadCacheRequest()
                    }
                }
                Event.Idle -> return@LaunchedEffect
            }
            viewModel.onEventHandled()
        }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        return@LifecycleEventObserver
                    }
                    if (!hasReadStoragePermission()) {
                        viewModel.onNoReadStoragePermission()
                    } else {
                        viewModel.onLoadCacheRequest()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    @Composable
    private fun CacheList(cacheList: List<Cache>) {
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
    private fun ListLoading() {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
        item: Cache,
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
        if (requestPermissionDeniedPermanently) {
            openAppSettings()
            return
        }
        legacyReadExternalStoragePermissionRequestLauncher.launch(READ_EXTERNAL_STORAGE)
    }

    private fun tryNavigateToSettingsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = getRequestReadExternalStorageIntent()
            readExternalStoragePermissionRequestLauncher.launch(intent)
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getRequestReadExternalStorageIntent(): Intent {
        val storageService = getSystemService(STORAGE_SERVICE) as StorageManager
        val intent = storageService.primaryStorageVolume.createOpenDocumentTreeIntent()
        val startDir = "Android%2Fdata";
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        var scheme = uri.toString()
        scheme = scheme.replace("/root/", "/document/")
        scheme += "%3A$startDir"
        uri = Uri.parse(scheme)
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        return intent
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }
}