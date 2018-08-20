package com.wallace.demo.app.utils

import com.wallace.demo.app.UnitSpec

/**
  * Created by 10192057 on 2018/7/30 0030.
  */
class JsonFormatterUnitSpec extends UnitSpec {
  teamID should "do unit test for JsonFormatter.format()" in {
    val jsonStr: String =
      """
        |{
        |    "test" : "1234567",
        |    "entry": {
        |        "jvm": "-Xms1G -Xmx1G",
        |        "environment": {}
        |    },
        |    "common": {
        |        "column": {
        |            "datetimeFormat": "yyyy-MM-dd HH:mm:ss",
        |            "timeFormat": "HH:mm:ss",
        |            "dateFormat": "yyyy-MM-dd",
        |            "extraFormats":["yyyyMMdd"],
        |            "timeZone": "GMT+8",
        |            "encoding": "utf-8"
        |        }
        |    },
        |    "core": {
        |        "dataXServer": {
        |            "address": "http://localhost:7001/api",
        |            "timeout": 10000,
        |            "reportDataxLog": false,
        |            "reportPerfLog": false
        |        },
        |        "transport": {
        |            "channel": {
        |                "class": "com.alibaba.datax.core.transport.channel.memory.MemoryChannel",
        |                "speed": {
        |                    "byte": -1,
        |                    "record": -1
        |                },
        |                "flowControlInterval": 20,
        |                "capacity": 512,
        |                "byteCapacity": 67108864
        |            },
        |            "exchanger": {
        |                "class": "com.alibaba.datax.core.plugin.BufferedRecordExchanger",
        |                "bufferSize": 32
        |            }
        |        },
        |        "container": {
        |            "job": {
        |                "reportInterval": 10000
        |            },
        |            "taskGroup": {
        |                "channel": 5
        |            },
        |            "trace": {
        |                "enable": "false"
        |            }
        |
        |        },
        |        "statistics": {
        |            "collector": {
        |                "plugin": {
        |                    "taskClass": "com.alibaba.datax.core.statistics.plugin.task.StdoutPluginCollector",
        |                    "maxDirtyNumber": 10
        |                }
        |            }
        |        }
        |    }
        |}
      """.stripMargin

    val res = JsonFormatter.format(jsonStr)

    log.info(res.toString())
    res.isEmpty shouldBe false
    res("test") shouldBe "1234567"
    res("common.column.datetimeFormat") shouldBe "yyyy-MM-dd HH:mm:ss"
    res("core.statistics.collector.plugin.maxDirtyNumber") shouldBe "10"
  }
}
