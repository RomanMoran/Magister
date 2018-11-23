package com.ngse.magister.date

import java.util.*


/**
 * Extension for {@link Calendar.DAY_OF_YEAR}
 */
var Calendar.dayOfYear : Int
    get() = get(Calendar.DAY_OF_YEAR)
    set(value) = set(Calendar.DAY_OF_YEAR, value)

/**
 * Extension for {@link Calendar.DAY_OF_MONTH}
 */
var Calendar.dayOfMonth : Int
    get() = get(Calendar.DAY_OF_MONTH)
    set(value) = set(Calendar.DAY_OF_MONTH, value)

/**
 * Extension for {@link Calendar.MONTH}
 */
var Calendar.month : Int
    get() = get(Calendar.MONTH)
    set(value) = set(Calendar.MONTH, value)

/**
 * Extension for {@link Calendar.YEAR}
 */
var Calendar.year : Int
    get() = get(Calendar.YEAR)
    set(value) = set(Calendar.YEAR, value)
