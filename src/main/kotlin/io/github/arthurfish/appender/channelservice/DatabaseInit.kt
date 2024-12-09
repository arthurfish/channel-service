package io.github.arthurfish.appender.channelservice

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseInit(
  private val jdbcTemplate: JdbcTemplate,
) {
  @PostConstruct
  @Order(1)
  fun createDatabaseIfNotExists() {
    val createTableSql = """
      DROP TABLE IF EXISTS channels;
      CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
      CREATE TABLE IF NOT EXISTS channels (
      channel_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
      channel_name VARCHAR(256) NOT NULL,
      owner_id UUID NOT NULL,
      members VARCHAR(1024) NOT NULL,
      inviting_code VARCHAR(64) NOT NULL DEFAULT uuid_generate_v4()::VARCHAR(64)
      );
    """.trimIndent()
    jdbcTemplate.execute(createTableSql)
    val log = LoggerFactory.getLogger(DatabaseInit::class.java)
    log.info("channel-service's psql database initialized.")
  }

  @PostConstruct
  @Order(2)
  private fun insertTestDataIfEmpty() {
    Thread.sleep(2000)
    val countSql = "SELECT COUNT(*) FROM channels"
    val count = jdbcTemplate.queryForObject(countSql, Int::class.java) ?: 0
    if (count == 0) {
      val insertSql = """
                INSERT INTO channels (channel_name, owner_id, members) VALUES 
                (?, ?::UUID, ?), (?, ?::UUID, ?), (?, ?::UUID, ?)
            """.trimIndent()

      val ownerId1 = "1ba81f78-2b1d-4b85-8053-5e8bdbb7c7e3"
      val ownerId2 = "2ba81f78-2b1d-4b85-8053-5e8bdbb7c7e3"
      val ownerId3 = "3ba81f78-2b1d-4b85-8053-5e8bdbb7c7e3"

      jdbcTemplate.update(insertSql,
        "Channel Fake", ownerId1, ownerId1,
        "Channel Folk", ownerId2, ownerId2,
        "Channel Fuck", ownerId3, ownerId3
      )

      val log = LoggerFactory.getLogger(DatabaseInit::class.java)
      log.info("Inserted test data into 'channels' table.")
    }
  }
}