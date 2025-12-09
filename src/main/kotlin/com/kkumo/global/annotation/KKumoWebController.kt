package com.kkumo.global.annotation

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Controller
@RequestMapping("/kkumo/v1")
annotation class KKumoWebController
