package com.littlemissadjective.hangitup

import java.io.File

class State {

    enum class MODE {
        SELF_PLACE, SUGGEST_PLACE
    }

    val perfectHeight = 1.4478

    lateinit var image: File

    var mode = MODE.SELF_PLACE
}