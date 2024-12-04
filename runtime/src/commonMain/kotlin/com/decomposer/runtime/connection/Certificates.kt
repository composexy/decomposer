package com.decomposer.runtime.connection

import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object Certificates {
    fun buildSSLContext(): Pair<SSLContext, X509TrustManager> {
        val trustManager = buildTrustManager()
        return Pair(
            SSLContext.getInstance("TLSv1.2").apply {
                init(arrayOf(), arrayOf(trustManager), null)
            },
            trustManager
        )
    }

    private fun buildTrustManager(): X509TrustManager {
        val keyStoreInputStream = javaClass.classLoader.getResourceAsStream("keystore.p12")
        val trustStore = keyStoreInputStream.use {
            val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
            keystore.load(it, "123456".toCharArray())
            keystore
        }
        val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        factory.init(trustStore)
        val result = factory.trustManagers
        check(result.isNotEmpty() && result[0] is X509TrustManager)
        return result[0] as X509TrustManager
    }
}
