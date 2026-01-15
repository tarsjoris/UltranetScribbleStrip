package be.t_ars.ultranetscribblestrip.xr18

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

data class XR18Info(val address: InetAddress, val name: String)

fun searchXR18(): XR18Info? {
    val socket = DatagramSocket().also { it.soTimeout = 5000 }
    try {
        socket.broadcast = false
        val payload = OSCMessage("/info").serialize()
        socket.send(
            DatagramPacket(
                payload, payload.size, InetAddress.getByName("192.168.0.238"), XR18OSCAPI.PORT
            )
        )

        val buffer = ByteArray(256)
        val udpPacket = DatagramPacket(buffer, buffer.size)
        socket.receive(udpPacket)
        val packet = parsePacket(udpPacket.data, udpPacket.length)
        if (packet is OSCMessage && packet.address == "/info") {
            val name = packet.getString(1)
            return XR18Info(udpPacket.address, name ?: "unknown")
        }
    } catch (e: Throwable) {
        Log.e("UltranetScribbleStrip", "Got error while searching XR18", e)
    } finally {
        socket.close();
    }

    return null
}