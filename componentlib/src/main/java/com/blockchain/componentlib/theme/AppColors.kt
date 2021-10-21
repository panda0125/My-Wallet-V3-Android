package com.blockchain.componentlib.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val Blue600 = Color(0XFF0C6CF2)
private val Blue400 = Color(0XFF619FF7)
private val Blue000 = Color(0XFFECF5FE)

private val Green600 = Color(0XFF00994C)
private val Green400 = Color(0XFF17CE73)
private val Green000 = Color(0XFFE6FAEC)

private val Red600 = Color(0XFFCF1726)
private val Red400 = Color(0XFFFF3344)
private val Red000 = Color(0XFFFFECEB)

private val Orange600 = Color(0XFFD46A00)
private val Orange400 = Color(0XFFFFA133)
private val Orange000 = Color(0XFFFFF6EB)

private val Grey900 = Color(0XFF121D33)
private val Grey800 = Color(0XFF353F52)
private val Grey700 = Color(0XFF50596B)
private val Grey600 = Color(0XFF677184)
private val Grey500 = Color(0XFF828B9E)
private val Grey400 = Color(0XFF98A1B2)
private val Grey300 = Color(0XFFB1B8C7)
private val Grey200 = Color(0XFFCCD2DE)
private val Grey100 = Color(0XFFDFE3EB)
private val Grey000 = Color(0XFFF0F2F7)

private val Dark900 = Color(0XFF0E121B)
private val Dark800 = Color(0XFF20242C)
private val Dark700 = Color(0XFF2C3038)
private val Dark600 = Color(0XFF3B3E46)
private val Dark500 = Color(0XFF4D515B)
private val Dark400 = Color(0XFF63676F)
private val Dark300 = Color(0XFF797D84)
private val Dark200 = Color(0XFF989BA1)
private val Dark100 = Color(0XFFB8B9BD)
private val Dark000 = Color(0XFFD2D4D6)

private val TierGold = Color(0XFFF5B73D)
private val TierSilver = Color(0XFFC2C9D6)

fun getLightColors() = SemanticColors(
    primary = Blue600,
    success = Green600,
    warning = Orange600,
    error = Red600,
    title = Grey900,
    body = Grey800,
    muted = Grey400,
    dark = Grey300,
    medium = Grey100,
    light = Grey000,
    isLight = true
)

fun getDarkColors() = SemanticColors(
    primary = Blue400,
    success = Green400,
    warning = Orange400,
    error = Red400,
    title = Grey900,
    body = Grey800,
    muted = Grey400,
    dark = Grey300,
    medium = Grey100,
    light = Grey000,
    isLight = false
)

val LocalColors = staticCompositionLocalOf { getLightColors() }