package com.vcore.proxy;

import android.net.VpnService;

/**
 * Proxy VPN service stub for the virtual framework.
 * <p>
 * Extends {@link android.net.VpnService} to allow guest applications that require
 * VPN capabilities to establish a VPN connection through the host application's
 * manifest entry. The actual VPN logic is handled by the guest application's
 * VPN service implementation.
 * </p>
 */
public class ProxyVpnService extends VpnService { }
