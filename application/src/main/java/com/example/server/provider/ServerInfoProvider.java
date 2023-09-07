package com.example.server.provider;

import com.example.server.constants.Properties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Component
@Log4j2
public class ServerInfoProvider {
	@Autowired
	private Environment env;


	public String getAddress () {
		return getIp() + ":" + env.getProperty(Properties.SERVER_PORT);
	}


	public String getIp () {
		String ip;
		try {
			List <String> ipList = getHostAddress(null);
			// default the first
			ip = (!ipList.isEmpty()) ? ipList.get(0) : "";
		} catch (Exception ex) {
			ip = "";
			log.warn("Utils get IP warn", ex);
		}
		return ip;
	}


	private List <String> getHostAddress (String interfaceName) throws SocketException {
		List <String>                  ipList     = new ArrayList <String>(5);
		Enumeration <NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface          ni         = interfaces.nextElement();
			Enumeration <InetAddress> allAddress = ni.getInetAddresses();
			while (allAddress.hasMoreElements()) {
				InetAddress address = allAddress.nextElement();
				if (address.isLoopbackAddress()) {
					// skip the loopback addr
					continue;
				}
				if (address instanceof Inet6Address) {
					// skip the IPv6 addr
					continue;
				}
				String hostAddress = address.getHostAddress();
				if (null == interfaceName) {
					ipList.add(hostAddress);
				} else if (interfaceName.equals(ni.getDisplayName())) {
					ipList.add(hostAddress);
				}
			}
		}
		return ipList;
	}
}
