package com.kkumo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
//@RequestMapping("/kkumo/v1")
class HelloController {
    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> {
        return ResponseEntity.ok().body("Hello World")
    }

    @GetMapping()
    fun hello2(): ResponseEntity<String> {
        return ResponseEntity.ok().body("Hello World!!!")
    }
}