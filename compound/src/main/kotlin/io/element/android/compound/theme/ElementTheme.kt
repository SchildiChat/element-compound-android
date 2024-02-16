/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.compound.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.element.android.compound.tokens.sc.ElTypographyTokens
import io.element.android.compound.tokens.sc.ExposedTypographyTokens
import io.element.android.compound.tokens.compoundColorsDark
import io.element.android.compound.tokens.compoundColorsLight
import io.element.android.compound.tokens.compoundTypography
import io.element.android.compound.tokens.generated.SemanticColors

/**
 * Inspired from https://medium.com/@lucasyujideveloper/54cbcbde1ace
 */
object ElementTheme {
    /**
     * The current [SemanticColors] provided by [ElementTheme].
     * These come from Compound and are the recommended colors to use for custom components.
     * In Figma, these colors usually have the `Light/` or `Dark/` prefix.
     */
    val colors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCompoundColors.current

    /**
     * The current Material 3 [ColorScheme] provided by [ElementTheme], coming from [MaterialTheme].
     * In Figma, these colors usually have the `M3/` prefix.
     */
    val materialColors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    /**
     * Compound [Typography] tokens. In Figma, these have the `Android/font/` prefix.
     */
    val typography: ExposedTypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    /**
     * Material 3 [Typography] tokens. In Figma, these have the `M3 Typography/` prefix.
     */
    val materialTypography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    /**
     * Returns whether the theme version used is the light or the dark one.
     */
    val isLightTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalCompoundColors.current.isLight
}

/* Global variables (application level) */
internal val LocalCompoundColors = staticCompositionLocalOf { compoundColorsLight }

internal val LocalTypography = staticCompositionLocalOf { ElTypographyTokens }

/**
 * Sets up the theme for the application, or a part of it.
 *
 * @param darkTheme whether to use the dark theme or not. If `true`, the dark theme will be used.
 * @param applySystemBarsUpdate whether to update the system bars color scheme or not when the theme changes. It's `true` by default.
 * This is specially useful when you want to apply an alternate theme to a part of the app but don't want it to affect the system bars.
 * @param lightStatusBar whether to use a light status bar color scheme or not. By default, it's the opposite of [darkTheme].
 * @param dynamicColor whether to enable MaterialYou or not. It's `false` by default.
 * @param compoundColors the [SemanticColors] to use. By default it'll automatically use the light or dark theme colors based on the system theme.
 * @param materialColors the Material 3 [ColorScheme] to use. It'll use either [materialColorSchemeLight] or [materialColorSchemeDark] by default.
 * @param typography the Material 3 [Typography] tokens to use. It'll use [compoundTypography] by default.
 * @param content the content to apply the theme to.
 */
@Composable
fun ElementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    applySystemBarsUpdate: Boolean = true,
    lightStatusBar: Boolean = !darkTheme,
    dynamicColor: Boolean = false, /* true to enable MaterialYou */
    compoundColors: SemanticColors = if (darkTheme) compoundColorsDark else compoundColorsLight,
    materialColors: ColorScheme = if (darkTheme) materialColorSchemeDark else materialColorSchemeLight,
    typography: Typography = compoundTypography,
    typographyTokens: ExposedTypographyTokens = ElTypographyTokens,
    content: @Composable () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    val currentCompoundColor = remember(darkTheme) {
        compoundColors.copy()
    }.apply { updateColorsFrom(compoundColors) }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> materialColors
    }

    val statusBarColorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        colorScheme
    }

    if (applySystemBarsUpdate) {
        LaunchedEffect(statusBarColorScheme, darkTheme, lightStatusBar) {
            systemUiController.applyTheme(
                colorScheme = statusBarColorScheme,
                darkTheme = darkTheme && !lightStatusBar
            )
        }
    }
    CompositionLocalProvider(
        LocalCompoundColors provides currentCompoundColor,
        LocalContentColor provides colorScheme.onSurface,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

internal fun SystemUiController.applyTheme(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
) {
    val useDarkIcons = !darkTheme
    setStatusBarColor(
        color = colorScheme.background
    )
    setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = useDarkIcons
    )
}
