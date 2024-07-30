package ru.tretyackov.todo.data.network

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
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import ru.tretyackov.todo.BuildConfig
import ru.tretyackov.todo.data.network.dto.CreateToDoItemDto
import ru.tretyackov.todo.data.network.dto.OperatedToDoItemDto
import ru.tretyackov.todo.data.network.dto.PatchToDoListDto
import ru.tretyackov.todo.data.network.dto.ToDoListDto
import ru.tretyackov.todo.data.network.dto.UpdateToDoItemDto
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

const val token = "ÒÎÊÅÍ"

interface ToDoListApi {
    @GET("list")
    suspend fun getToDoList(): ToDoListDto

    @POST("list")
    suspend fun add(
        @Header("X-Last-Known-Revision") revision: Int,
        @Query("id") id: String,
        @Body createToDoItemDto: CreateToDoItemDto
    ): OperatedToDoItemDto

    @PUT("list/{id}")
    suspend fun update(
        @Header("X-Last-Known-Revision") revision: Int,
        @Path("id") id: String,
        @Body updateToDoItemDto: UpdateToDoItemDto
    ): OperatedToDoItemDto

    @DELETE("list/{id}")
    suspend fun delete(
        @Header("X-Last-Known-Revision") revision: Int,
        @Path("id") id: String
    ): OperatedToDoItemDto

    @PATCH("list")
    suspend fun patch(
        @Header("X-Last-Known-Revision") revision: Int,
        @Body patchToDoListDto: PatchToDoListDto
    ): ToDoListDto
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
        while (!response.isSuccessful && retryCount > 0) {
            response.close()
            response = chain.proceed(request)
            retryCount--
        }
        return response
    }
}

@Module
object ApiModule {
    @Singleton
    @Provides
    fun provideApi(): ToDoListApi = createApi()
    private fun createApi(): ToDoListApi {
        val client = OkHttpClient().newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor())
            .addInterceptor(AuthInterceptor())
            .build()
        return Retrofit.Builder().baseUrl(BuildConfig.SERVER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ToDoListApi::class.java)
    }
}