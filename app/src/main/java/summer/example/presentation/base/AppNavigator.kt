package summer.example.presentation.base

import summer.example.entity.Framework

interface AppNavigator {
    fun toFrameworkDetails(framework: Framework)
}