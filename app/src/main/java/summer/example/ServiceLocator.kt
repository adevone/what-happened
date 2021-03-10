package summer.example

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import summer.example.domain.about.GetAbout
import summer.example.domain.basket.BasketController
import summer.example.domain.basket.CoroutinesBasketController
import summer.example.domain.frameworks.GetAllFrameworkItems
import summer.example.domain.frameworks.GetSpring
import summer.example.domain.frameworks.GetSummer
import summer.example.presentation.AboutViewModel
import summer.example.presentation.BasketViewModel
import summer.example.presentation.FrameworkDetailsViewModel
import summer.example.presentation.FrameworksViewModel
import summer.example.presentation.MainViewModel
import summer.example.recording.SerializationHappenedEventRecorder

object ServiceLocator {

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    val happenedEventRecorder = SerializationHappenedEventRecorder(json)

    private val basketController: BasketController = CoroutinesBasketController()

    fun aboutViewModel() = AboutViewModel(GetAbout(httpClient(), json), Dispatchers.Main)

    fun basketViewModel() = BasketViewModel(basketController, Dispatchers.Main)

    fun frameworkDetailsViewModel() = FrameworkDetailsViewModel(Dispatchers.Main)

    fun frameworksViewModel() = FrameworksViewModel(
        basketController,
        GetAllFrameworkItems(GetSpring(), GetSummer(), basketController),
        Dispatchers.Main
    )

    fun mainViewModel() = MainViewModel(Dispatchers.Main)

    fun httpClient() = HttpClient(OkHttp)
}