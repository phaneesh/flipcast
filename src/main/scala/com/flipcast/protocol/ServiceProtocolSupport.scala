package com.flipcast.protocol

import com.flipcast.model.requests.SmsUnicastRequest
import spray.json._
import spray.httpx.SprayJsonSupport
import com.flipcast.model.responses._
import com.flipcast.push.protocol.{PushHistoryDataProtocol, DeviceDataProtocol}
import com.flipcast.push.model.{PushHistoryData, DeviceOperatingSystemType, DeviceData}
import com.flipcast.model.responses.StatusCheckResponse
import com.flipcast.model.responses.InRotationResponse
import com.flipcast.model.responses.PingServiceResponse
import com.flipcast.model.responses.OutOfRotationResponse
import com.flipcast.push.model.responses.DeviceDetailsRegisterResponse
import com.flipcast.model.responses.UpdatePushConfigResponse

import scala.util.Try

/**
 * JSON protocol support for all models (request/response) used in the service
 *
 * @author Phaneesh Nagaraja
 */
trait ServiceProtocolSupport extends DefaultJsonProtocol
                  with SprayJsonSupport with DeviceDataProtocol
                  with PushHistoryDataProtocol  {

  /**
   * JSON format for ping response
   */
  implicit val PingServiceResponseFormat = jsonFormat3(PingServiceResponse)

  /**
   * JSON format for status check
   */
  implicit val StatusCheckResponseFormat = jsonFormat1(StatusCheckResponse)

  /**
   * JSON format for push configuration update
   */
  implicit val UpdatePushConfigResponseFormat = jsonFormat1(UpdatePushConfigResponse)

  /**
   * JSON format for in rotation response
   */
  implicit val InRotationResponseFormat = jsonFormat1(InRotationResponse)

  /**
   * JSON format for out of rotation response
   */
  implicit val OutOfRotationResponseFormat = jsonFormat1(OutOfRotationResponse)

  /**
   * JSON format for unicast success response
   */
  implicit val UnicastSuccessResponseFormat = jsonFormat2(UnicastSuccessResponse)

  /**
   * JSON format for multicast success response
   */
  implicit val MulticastSuccessResponseFormat = jsonFormat2(MulticastSuccessResponse)

  /**
    * JSON format for sms unicast success response
    */
  implicit val SmsUnicastSuccessResponseFormat = jsonFormat1(SmsUnicastSuccessResponse)

  /**
   * JSON format for device details registration request
   */
  implicit object DeviceDetailsRegisterResponseFormat extends RootJsonFormat[DeviceDetailsRegisterResponse] {

    def write(obj: DeviceDetailsRegisterResponse) = {
      JsObject(
        "device" -> obj.deviceData.toJson
      )
    }

    def read(json: JsValue) = {
      val device = json.asJsObject.fields.contains("device") match {
        case true =>
          Option(json.asJsObject.fields("device").convertTo[DeviceData])
        case false => None
      }
      DeviceDetailsRegisterResponse(deviceData = device.getOrElse(DeviceData("Unknown","Unknown","Unknown",
        DeviceOperatingSystemType.Unknown,"Unknown","Unknown","Unknown","Unknown","Unknown")))
    }
  }

  /**
   * JSON format for push history request
   */
  implicit object GetAllPushHistoryResponseFormat extends RootJsonFormat[GetAllPushHistoryResponse] {

    def write(obj: GetAllPushHistoryResponse) = {
        obj.data.toJson
    }

    def read(json: JsValue) = {
      val data = json.convertTo[PushHistoryData]
      GetAllPushHistoryResponse(configName = "NA", data)
    }
  }

  implicit object SmsUnicastRequestFormat extends JsonFormat[SmsUnicastRequest] {

    def read(json: JsValue) = {
      val configName = json.asJsObject.fields.contains("configName") match {
        case true =>
          json.asJsObject.fields("configName") match {
            case s: JsString => s.value
            case _ => "NA"
          }
        case false => "NA"
      }
      val provider = json.asJsObject.fields.contains("provider") match {
        case true =>
          json.asJsObject.fields("provider") match {
            case s: JsString => s.value
            case _ => "NA"
          }
        case false => "NA"
      }
      val to = json.asJsObject.fields.contains("to") match {
        case true =>
          json.asJsObject.fields("to") match {
            case s: JsString => s.value
            case _ => "NA"
          }
        case false => "NA"
      }
      val message = json.asJsObject.fields.contains("message") match {
        case true =>
          json.asJsObject.fields("message") match {
            case s: JsString => s.value
            case s: JsObject => s.compactPrint
            case _ => "NA"
          }
        case false => "NA"
      }
      SmsUnicastRequest(configName, provider, message, to)
    }

    def write(value : SmsUnicastRequest) = {
      JsObject(
        "configName" -> JsString(value.configName),
        "provider" -> JsString(value.provider),
        "to" -> JsString(value.to),
        "message" -> Try(JsonParser(value.message).asJsObject).getOrElse(JsString(value.message))
      )
    }
  }

}
