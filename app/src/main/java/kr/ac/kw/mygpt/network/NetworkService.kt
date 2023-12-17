package kr.ac.kw.mygpt.network

import com.google.gson.GsonBuilder
import kr.ac.kw.mygpt.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkService {
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val okhttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val gptApi: GPTApi = retrofit.create(GPTApi::class.java)
}