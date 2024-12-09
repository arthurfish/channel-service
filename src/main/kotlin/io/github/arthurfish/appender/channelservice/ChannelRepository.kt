package io.github.arthurfish.appender.channelservice

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.ResultSet

class ChannelRowMapper : RowMapper<Map<String, String>> {
  override fun mapRow(rs: ResultSet, rowNum: Int): Map<String, String> {
    val map = mapOf(
      "channel_id" to rs.getString("channel_id"),
      "channel_name" to rs.getString("channel_name"),
      "owner_id" to rs.getString("owner_id"),
      "members" to rs.getString("members"),
      "inviting_code" to rs.getString("inviting_code"))
    return map;
  }
}


@Repository
class ChannelRepository(
  private val jdbcClient: JdbcClient,
){
  fun createChannel(ownerId: String, name: String = "untitled-channel"): String{
    val sql = "INSERT INTO channels (channel_name, owner_id, members) values (?, ?, ?) returning channel_id"
    val generatedIdHolder = GeneratedKeyHolder()
    jdbcClient.sql(sql).params(name, ownerId, ownerId).update(generatedIdHolder)
    return generatedIdHolder.getKeyAs(String::class.java)!!
  }

  fun readChannelInfo(channelId: String): Map<String, String> {
    val sql = "SELECT * FROM channels WHERE channel_id = ?"
    val result = jdbcClient.sql(sql).params(channelId).query(ChannelRowMapper()).single()
    return result;
  }


  fun updateChannelName(channelId: String, name: String){
    val sql = "UPDATE channels SET channel_name = :channel_name WHERE channel_id = :channel_id"
    val queryResult = jdbcClient.sql(sql)
      .params("channel_id", channelId)
      .params("channel_name", name)
      .update()
  }
  fun updateChannelMembers(channelId: String, members: String) {
    val sql = "UPDATE channels SET members = ? WHERE channel_id = ?"
    jdbcClient.sql(sql)
      .param(members) // Assuming members are stored as a comma-separated string
      .param(channelId)
      .update()
  }

  fun deleteChannel(channelId: String) {
    val sql = "DELETE FROM channels WHERE channel_id = ?"
    jdbcClient.sql(sql)
      .param(channelId)
      .update()
  }
}