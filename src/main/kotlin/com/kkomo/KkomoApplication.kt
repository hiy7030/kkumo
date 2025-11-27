package com.kkomo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KkomoApplication

fun main(args: Array<String>) {
	runApplication<KkomoApplication>(*args)
}
