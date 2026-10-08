# WebRTC internals and deployment guidance

**ICE** gathers local/server-reflexive/relay candidates and nominates an available path. With this architecture the browser connects to the LiveKit SFU, not peer-to-peer. Server-advertised candidates must be reachable from every client. `--node-ip 127.0.0.1` is for same-host local demos only.

**DTLS-SRTP**: browser and SFU negotiate DTLS fingerprints/keys, derive SRTP material and encrypt/decrypt media on each hop. TLS on the WebSocket/HTTP signalling endpoint must be configured separately in production.

**RTP/RTCP**: SFU forwards RTP and processes packet feedback for retransmits, key-frame requests, stats and congestion. Browsers expose some aggregate stats using WebRTC getStats. Inspect RTP NACK/PLI, sender/receiver reports and transport-cc in `chrome://webrtc-internals`, packet captures, and media-server observability.

**Congestion control**: WebRTC sender bitrate adaptation, transport feedback and SFU quality/layer selection are performed by LiveKit and the browser. App-layer bitrate caps and quality preferences may be layered on top. This repo does not implement Google Congestion Control (GCC) from scratch.

**SDP**: LiveKit SDK negotiates offer/answer, codec capabilities, bundle, media directions and candidate exchange. It is not exposed as a standalone Java SDP REST endpoint.

**TURN**: standalone coturn listens at UDP/TCP 3478 and relays UDP 49160–49170. This container serves protocol exploration only; LiveKit's SFU does not automatically use it. The correct production path is to follow the current LiveKit self-hosting TURN guidance and integrate supported relay advertisement, TLS certs, ingress, firewall and cloud host candidate settings. A client-supplied STUN/TURN entry does not magically cause a LiveKit SFU to use an external relay.

**Internet production checklist**: verified hostname, WSS/TLS, egress/ingress UDP media, STUN/TURN relays, public IP advertising, authentication, cert rotation, Redis auth, metrics, load testing, region routing, QoS and rate limits.
