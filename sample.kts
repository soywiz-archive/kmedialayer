#!/usr/bin/env kscript

// Works with: https://github.com/holgerbrandl/kscript

@file:KotlinOpts("-J-XstartOnFirstThread")
@file:DependsOn("com.soywiz:kmedialayer:0.0.2")
@file:MavenRepository("soywiz-bintray","https://dl.bintray.com/soywiz/soywiz/" )

import com.soywiz.kmedialayer.*

Kml.application(WindowConfig(), object : KMLWindowListener() {
    override fun render(gl: KmlGl) {
        super.render(gl)
    }
})
