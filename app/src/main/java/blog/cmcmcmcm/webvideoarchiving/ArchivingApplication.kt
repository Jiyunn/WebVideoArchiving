package blog.cmcmcmcm.webvideoarchiving

import android.app.Application
import com.facebook.stetho.Stetho
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import io.realm.Realm

class ArchivingApplication : Application() {

    private var userAgent=""

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector( RealmInspectorModulesProvider.builder(this).build())
                        .build()
        )

        //Generate a User-Agent string that should be sent with HTTP requests.
        userAgent = Util.getUserAgent(this, getString(R.string.app_name))
    }

    fun buildDataSourceFactory(listener: TransferListener<in DataSource>?) : DataSource.Factory {
        return DefaultDataSourceFactory(this, listener, buildHttpDataSourceFactory(listener))
    }

    private fun buildHttpDataSourceFactory(listener: TransferListener<in DataSource>?) : HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent, listener)
    }
}