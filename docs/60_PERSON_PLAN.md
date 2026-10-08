# Sixty-participant acceptance plan

## Implemented in this incremental update

- LiveKit default room capacity of 60.
- Occupancy indicator in the conference interface.
- Adaptive-stream and dynacast switches in the React SDK room options.

## Must validate before claiming 60-person support

1. Deploy the SFU with a publicly routable IP, UDP connectivity, TLS and TURN fallback.
2. Use a WebRTC media load generator and real browser clients; include 60 publishers/participants and realistic subscription limits.
3. Track server CPU/network, bitrate, jitter, RTT, packet loss, freeze duration, reconnect and join latency.
4. Test active speaker, 5x5 paginated gallery, stage mode, simulcast/SVC and selective subscriptions.
5. Test 61st join rejection, concurrent join races, departures and reconnects.
6. Add host/moderator roles, waiting room approval, identity/authentication, room history and rate limits.
7. Add transport inspector for safe SDP snapshots, ICE timelines, DTLS state, RTCP and bitrate history; avoid storing secrets or decrypted media.

The existing `VideoConference` component supplies baseline conferencing controls; it is not a finished Microsoft Teams interface.
