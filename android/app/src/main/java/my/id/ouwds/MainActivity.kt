package my.id.ouwds

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private var pick: ValueCallback<Array<Uri>>? = null
    private var camUri: Uri? = null

    private val chooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val picked = if (r.resultCode == RESULT_OK) (r.data?.data ?: camUri) else null
        pick?.onReceiveValue(picked?.let { arrayOf(it) })
        pick = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        web = WebView(this).also { setContentView(it) }
        web.settings.apply {
            javaScriptEnabled = true; domStorageEnabled = true
            allowFileAccess = false; allowContentAccess = false
            mediaPlaybackRequiresUserGesture = false
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest) = loader.shouldInterceptRequest(r.url)
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                if (r.url.host == "appassets.androidplatform.net") return false
                startActivity(Intent(Intent.ACTION_VIEW, r.url)); return true
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(w: WebView, cb: ValueCallback<Array<Uri>>, p: FileChooserParams): Boolean {
                pick?.onReceiveValue(null); pick = cb
                val dir = File(cacheDir, "photos").apply { mkdirs() }
                camUri = FileProvider.getUriForFile(this@MainActivity, "$packageName.files", File.createTempFile("p_", ".jpg", dir))
                val cam = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, camUri).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val intent = if (p.isCaptureEnabled) cam else Intent.createChooser(
                    Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*"), null
                ).putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cam))
                return try { chooser.launch(intent); true } catch (e: Exception) { pick = null; false }
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (web.canGoBack()) web.goBack() else finish() }
        })
        if (savedInstanceState == null) web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
        else web.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(out: Bundle) { super.onSaveInstanceState(out); web.saveState(out) }
}
