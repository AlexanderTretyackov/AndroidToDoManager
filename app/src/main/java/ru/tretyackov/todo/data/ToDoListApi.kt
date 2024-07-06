package ru.tretyackov.todo.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import ru.tretyackov.todo.utilities.ConnectivityMonitor
import ru.tretyackov.todo.utilities.IConnectivityMonitor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

const val token = "ТОКЕН"

interface ToDoListApi {
    @GET("list")
    suspend fun getToDoList() : ToDoListDto
    @POST("list")
    suspend fun add(@Header("X-Last-Known-Revision") revision: Int,
                    @Query("id") id: String,
                    @Body createToDoItemDto: CreateToDoItemDto) : OperatedToDoItemDto
    @PUT("list/{id}")
    suspend fun update(@Header("X-Last-Known-Revision") revision: Int, @Path("id") id: String, @Body updateToDoItemDto: UpdateToDoItemDto) : OperatedToDoItemDto
    @DELETE("list/{id}")
    suspend fun delete(@Header("X-Last-Known-Revision") revision: Int, @Path("id") id: String) : OperatedToDoItemDto
}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var retryCount = 3
        var response = chain.proceed(request)
        while(!response.isSuccessful && retryCount > 0)
        {
            response.close()
            response = chain.proceed(request)
            retryCount--
        }
        return response
    }
}

@Module
object ApiModule{
    @Singleton
    @Provides
    fun provideApi(): ToDoListApi = createApi()
    private fun createApi() : ToDoListApi {
        val BASE_URL = "https://hive.mrdekk.ru/todo/"
        val client = OkHttpClient().newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor())
            .addInterceptor(AuthInterceptor())
            .build()
        return Retrofit.Builder().baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ToDoListApi::class.java)
    }
}

@Module
interface ConnectivityModule{
    @Singleton
    @Binds
    fun bindConnectivityMonitor(impl : ConnectivityMonitor): IConnectivityMonitor
}
