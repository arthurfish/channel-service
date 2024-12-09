package io.github.arthurfish.appender.channelservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJdbcRepositories
class ChannelServiceApplication

fun main(args: Array<String>) {
  runApplication<ChannelServiceApplication>(*args)
}
