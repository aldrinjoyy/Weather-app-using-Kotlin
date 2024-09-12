package com.example.myapplication // Update the package name to match your project

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data models for weather response
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Float,
    val humidity: Int
)

data class Weather(
    val description: String
)

// Retrofit API service interface
interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

// Retrofit instance and service object
object WeatherApi {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeatherApp() {
    var city by remember { mutableStateOf("Mangalore") }
    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Weather App") })
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Enter City") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            weatherData = WeatherApi.service.getCurrentWeather(city, "4842f1f06afeaec8c49053a2e18f5198")
                        } catch (e: Exception) {
                            errorMessage = "Failed to fetch weather data. Please try again."
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("Get Weather")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Text("Loading...", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                } else if (errorMessage != null) {
                    Text(errorMessage!!, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                } else if (weatherData != null) {
                    Text(
                        text = "Temperature: ${weatherData!!.main.temp}°C",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Humidity: ${weatherData!!.main.humidity}%",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Description: ${weatherData!!.weather[0].description}",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}
