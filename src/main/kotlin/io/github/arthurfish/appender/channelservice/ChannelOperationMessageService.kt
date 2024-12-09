package io.github.arthurfish.appender.channelservice

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

const val CHANNEL_ID_STR = "channel_id"
const val CHANNEL_OPERATION_STR = "channel_operation"
const val CHANNEL_NAME_STR = "channel_name"
const val CHANNEL_OPERATION_RESULT_STR = "channel_operation_result"
const val MEMBERS_STR = "members"
const val SUCCESS_STR = "success"
@Service
class ChannelOperationMessageService(
  private val channelRepository: ChannelRepository,
  private val rabbitTemplate: RabbitTemplate
) {
  val log = LoggerFactory.getLogger(ChannelOperationMessageService::class.java)

  @RabbitListener(queues = ["#{channelOperationQueue.name}"])
  fun processChannelOperationMessage(message: Map<String, String>) = try {
    val channelOperation = message[CHANNEL_OPERATION_STR]!!
    val channelId = message[CHANNEL_ID_STR]!!
    val responseMessage = when (channelOperation) {
      "create" -> createChannel(message)
      "read" -> readChannel(message)
      "update" -> updateChannel(message)
      "delete" -> deleteChannel(message)
      "invite" -> inviteToChannel(message)
      "join" -> joinToChannel(message)
      else -> throw AssertionError("Operation do not support!.")
    }
    rabbitTemplate.convertAndSend(
      "appender-core-exchange",
      "default.key",
      responseMessage
    ){
      message -> responseMessage.forEach{(k, v) -> message.messageProperties.headers[k] = v}
      message
    }
  } catch (e: Exception) {
    e.printStackTrace()
    throw AssertionError("Processing Failed in channel-service. message: $message")
  }


  fun createChannel(message: Map<String, String>): Map<String, String> {
    log.info("Channel is expected to create.")
    val userId = message["user_id"]!!
    val channelName = message[CHANNEL_NAME_STR] ?: "untitled-channel"
    val generatedId = channelRepository.createChannel(userId, channelName)
    val resultMessage = message
      .minus(CHANNEL_OPERATION_STR)
      .plus(CHANNEL_OPERATION_RESULT_STR to SUCCESS_STR)
      .plus(CHANNEL_ID_STR to generatedId)
    log.info("Created new channel with user_id: $userId")
    return resultMessage
  }

  fun readChannel(message: Map<String, String>): Map<String, String> {
    log.info("Channel is expected to read.")
    return channelRepository.readChannelInfo(channelId=message[CHANNEL_ID_STR]!!)
      .plus(message)
      .minus(CHANNEL_OPERATION_STR)
      .plus(CHANNEL_OPERATION_RESULT_STR to SUCCESS_STR)
  }

  fun updateChannel(message: Map<String, String>): Map<String, String> {
    log.info("Channel is expected to be updated.")
    if (message.containsKey(CHANNEL_NAME_STR))
      channelRepository.updateChannelName(channelId=message[CHANNEL_ID_STR]!!, name=message[CHANNEL_NAME_STR]!!)
    if (message.containsKey(MEMBERS_STR))
      channelRepository.updateChannelMembers(channelId = message[CHANNEL_ID_STR]!!, members = message[MEMBERS_STR]!!)
    return message
      .minus(CHANNEL_OPERATION_STR)
      .plus(CHANNEL_OPERATION_RESULT_STR to SUCCESS_STR)
  }

  fun deleteChannel(message: Map<String, String>): Map<String, String> {
    log.info("Channel is expected to be deleted.")
    channelRepository.deleteChannel(channelId=message[CHANNEL_ID_STR]!!)
    return message
      .minus(CHANNEL_OPERATION_STR)
      .plus(CHANNEL_OPERATION_RESULT_STR to SUCCESS_STR)
  }

  fun inviteToChannel(message: Map<String, String>): Map<String, String> {
    log.info("User is expected to be invited to the channel.")
    return mapOf("" to "")
  }

  fun joinToChannel(message: Map<String, String>): Map<String, String> {
    log.info("User is expected to be join the channel.")
    return mapOf("" to "")
  }
}