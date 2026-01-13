package be.t_ars.ultranetscribblestrip.xr18

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

data class UltranetChannelInfo(val name: String, val color: Int)

suspend fun getUltranetInfo(): Array<UltranetChannelInfo>? {
    Log.i("UltranetScribbleStrip", "Searching for XR18")
    val address = searchXR18()
    if (address != null) {
        Log.i("UltranetScribbleStrip", "Found at $address")
        return loadData(address)
    } else {
        return null
    }
}

private suspend fun loadData(address: InetAddress): Array<UltranetChannelInfo>? {
    val xR18OSCAPI = XR18OSCAPI(address)
    try {
        return requestData(xR18OSCAPI)
    } catch (e: Throwable) {
        Log.e("UltranetScribbleStrip", "Got error while searching XR18", e)
    } finally {
        xR18OSCAPI.stop();
    }
    return null
}

private suspend fun requestData(xR18OSCAPI: XR18OSCAPI): Array<UltranetChannelInfo> {
    val routingSources = IntArray(16) { -1 }
    val routingSourceNames = Array(XR18OSCAPI.ROUTING_SOURCE_COUNT) {
        when (it) {
            in XR18OSCAPI.ROUTING_SOURCE_CHANNEL1..XR18OSCAPI.ROUTING_SOURCE_CHANNEL16
                -> "CH ${it - XR18OSCAPI.ROUTING_SOURCE_CHANNEL1 + 1}"

            XR18OSCAPI.ROUTING_SOURCE_AUX_L
                -> "AUX L"

            XR18OSCAPI.ROUTING_SOURCE_AUX_R
                -> "AUX R"

            in XR18OSCAPI.ROUTING_SOURCE_RTN1_L..XR18OSCAPI.ROUTING_SOURCE_RTN4_R
                -> "Rtn ${(it - XR18OSCAPI.ROUTING_SOURCE_RTN1_L).div(2) + 1} ${
                if ((it - XR18OSCAPI.ROUTING_SOURCE_RTN1_L).mod(
                        2
                    ) == 0
                ) "0" else "1"
            }"

            in XR18OSCAPI.ROUTING_SOURCE_BUS1..XR18OSCAPI.ROUTING_SOURCE_BUS6
                -> "Bus ${it - XR18OSCAPI.ROUTING_SOURCE_BUS1 + 1}"

            in XR18OSCAPI.ROUTING_SOURCE_SEND1..XR18OSCAPI.ROUTING_SOURCE_SEND4
                -> "FxSnd ${it - XR18OSCAPI.ROUTING_SOURCE_SEND1 + 1}"

            XR18OSCAPI.ROUTING_SOURCE_LR_L
                -> "LR L"

            XR18OSCAPI.ROUTING_SOURCE_LR_R
                -> "LR R"

            in XR18OSCAPI.ROUTING_SOURCE_DCA1..XR18OSCAPI.ROUTING_SOURCE_DCA4
                -> "DCA ${it - XR18OSCAPI.ROUTING_SOURCE_DCA1 + 1}"

            in XR18OSCAPI.ROUTING_SOURCE_USB1..XR18OSCAPI.ROUTING_SOURCE_USB14
                -> "USB ${it - XR18OSCAPI.ROUTING_SOURCE_USB1 + 1}"

            else
                -> ""
        }
    }
    val routingSourceColors = IntArray(XR18OSCAPI.ROUTING_SOURCE_COUNT)

    val semaphore = Semaphore(0)
    val listener: IOSCListener = object : IOSCListener {
        override suspend fun p16RoutingSource(routing: Int, source: Int) {
            routingSources[routing - 1] = source
            semaphore.release()
        }

        override suspend fun channelName(channel: Int, name: String) {
            if (channel == 17) {
                routingSourceNames[XR18OSCAPI.ROUTING_SOURCE_AUX_L] = "$name L"
                routingSourceNames[XR18OSCAPI.ROUTING_SOURCE_AUX_R] = "$name R"
            } else {
                routingSourceNames[channel - 1 + XR18OSCAPI.ROUTING_SOURCE_CHANNEL1] = name
            }
            semaphore.release()
        }

        override suspend fun channelColor(channel: Int, color: Int) {
            if (channel == 17) {
                routingSourceColors[XR18OSCAPI.ROUTING_SOURCE_AUX_L] = color
                routingSourceColors[XR18OSCAPI.ROUTING_SOURCE_AUX_R] = color
            } else {
                routingSourceColors[channel - 1 + XR18OSCAPI.ROUTING_SOURCE_CHANNEL1] = color
            }
            semaphore.release()
        }

        override suspend fun returnName(returnChannel: Int, name: String) {
            val leftIndex = (returnChannel - 1) * 2 + XR18OSCAPI.ROUTING_SOURCE_RTN1_L
            routingSourceNames[leftIndex] = "$name L"
            routingSourceNames[leftIndex + 1] = "$name R"
            semaphore.release()
        }

        override suspend fun returnColor(returnChannel: Int, color: Int) {
            val leftIndex = (returnChannel - 1) * 2 + XR18OSCAPI.ROUTING_SOURCE_RTN1_L
            routingSourceColors[leftIndex] = color
            routingSourceColors[leftIndex + 1] = color
            semaphore.release()
        }

        override suspend fun busName(bus: Int, name: String) {
            routingSourceNames[bus - 1 + XR18OSCAPI.ROUTING_SOURCE_BUS1] = name
            semaphore.release()
        }

        override suspend fun busColor(bus: Int, color: Int) {
            routingSourceColors[bus - 1 + XR18OSCAPI.ROUTING_SOURCE_BUS1] = color
            semaphore.release()
        }

        override suspend fun fxSendName(fxSend: Int, name: String) {
            routingSourceNames[fxSend - 1 + XR18OSCAPI.ROUTING_SOURCE_SEND1] = name
            semaphore.release()
        }

        override suspend fun fxSendColor(fxSend: Int, color: Int) {
            routingSourceColors[fxSend - 1 + XR18OSCAPI.ROUTING_SOURCE_SEND1] = color
            semaphore.release()
        }

        override suspend fun lrName(name: String) {
            routingSourceNames[XR18OSCAPI.ROUTING_SOURCE_LR_L] = "$name L"
            routingSourceNames[XR18OSCAPI.ROUTING_SOURCE_LR_R] = "$name R"
            semaphore.release()
        }

        override suspend fun lrColor(color: Int) {
            routingSourceColors[XR18OSCAPI.ROUTING_SOURCE_LR_L] = color
            routingSourceColors[XR18OSCAPI.ROUTING_SOURCE_LR_R] = color
            semaphore.release()
        }
    }
    xR18OSCAPI.addListener(listener)
    coroutineScope { launch { xR18OSCAPI.handleResponses() } }
    repeat(XR18OSCAPI.ROUTING_COUNT) {
        requestParameter(semaphore) { xR18OSCAPI.requestP16RoutingSource(it + 1) }
    }
    repeat(XR18OSCAPI.CHANNEL_COUNT) {
        requestParameter(semaphore) { xR18OSCAPI.requestChannelName(it + 1) }
        requestParameter(semaphore) { xR18OSCAPI.requestChannelColor(it + 1) }
    }
    repeat(XR18OSCAPI.RETURN_COUNT) {
        requestParameter(semaphore) { xR18OSCAPI.requestReturnName(it + 1) }
        requestParameter(semaphore) { xR18OSCAPI.requestReturnColor(it + 1) }
    }
    repeat(XR18OSCAPI.BUS_COUNT) {
        requestParameter(semaphore) { xR18OSCAPI.requestBusName(it + 1) }
        requestParameter(semaphore) { xR18OSCAPI.requestBusColor(it + 1) }
    }
    repeat(XR18OSCAPI.FXSEND_COUNT) {
        requestParameter(semaphore) { xR18OSCAPI.requestFXSendName(it + 1) }
        requestParameter(semaphore) { xR18OSCAPI.requestFXSendColor(it + 1) }
    }
    requestParameter(semaphore) { xR18OSCAPI.requestLRName() }
    requestParameter(semaphore) { xR18OSCAPI.requestLRColor() }

    return Array(XR18OSCAPI.ROUTING_COUNT) { index ->
        val routingSource = routingSources[index]
        if (routingSource == -1)
            UltranetChannelInfo("-", 0)
        else
            UltranetChannelInfo(
                routingSourceNames[routingSource],
                routingSourceColors[routingSource]
            )
    }
}

private fun requestParameter(semaphore: Semaphore, request: () -> Unit) {
    var tries = 0
    do {
        if (++tries > 10) {
            Log.e("UltranetScribbleStrip", "Could not request parameter")
            continue
        }
        request()
    } while (!semaphore.tryAcquire(1, TimeUnit.SECONDS))
}