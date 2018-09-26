package com.littlemissadjective.hangitup

import java.io.File

class State {

    enum class Mode {
        SELF_PLACE, SUGGEST_PLACE
    }

    lateinit var image: File

    var mode = Mode.SELF_PLACE
}