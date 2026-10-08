"use client";

export type ProtocolEvent = { at: string; type: string; detail: string };

declare global {
  interface Window {
    __signalRoomTrace?: ProtocolEvent[];
    __signalRoomTap?: boolean;
  }
}

function add(type: string, detail: string): void {
  if (typeof window === "undefined") return;
  window.__signalRoomTrace = [
    { at: new Date().toISOString(), type, detail },
    ...(window.__signalRoomTrace ?? []),
  ].slice(0, 120);
  window.dispatchEvent(new Event("signalroom-protocol"));
}

function reportDescription(method: string, description?: RTCSessionDescriptionInit): void {
  if (!description?.sdp) return;
  const sanitized = description.sdp
    .split(/\r?\n/)
    .filter((line) =>
      !line.startsWith("a=ice-pwd:") &&
      !line.startsWith("a=ice-ufrag:") &&
      !line.startsWith("a=fingerprint:") &&
      !line.startsWith("a=candidate:")
    )
    .slice(0, 80)
    .join("\n");
  add(`SDP ${method}`, `${description.type}\n${sanitized}`);
}

/** Developer-only metadata tracing: never store media keys, SDP credentials or full ICE candidates. */
export function installProtocolTap(): void {
  if (typeof window === "undefined" || window.__signalRoomTap || typeof RTCPeerConnection === "undefined") return;
  window.__signalRoomTap = true;
  const proto = RTCPeerConnection.prototype;
  const originalLocal = proto.setLocalDescription;
  const originalRemote = proto.setRemoteDescription;
  const originalCandidate = proto.addIceCandidate;

  proto.setLocalDescription = function (this: RTCPeerConnection, description?: RTCSessionDescriptionInit): Promise<void> {
    reportDescription("setLocalDescription", description);
    return Reflect.apply(originalLocal, this, description === undefined ? [] : [description]) as Promise<void>;
  } as typeof proto.setLocalDescription;

  proto.setRemoteDescription = function (this: RTCPeerConnection, description: RTCSessionDescriptionInit): Promise<void> {
    reportDescription("setRemoteDescription", description);
    return Reflect.apply(originalRemote, this, [description]) as Promise<void>;
  } as typeof proto.setRemoteDescription;

  proto.addIceCandidate = function (this: RTCPeerConnection, candidate?: RTCIceCandidateInit | null): Promise<void> {
    if (candidate) {
      const text = candidate.candidate ?? "";
      const kind = text.match(/typ (host|srflx|relay|prflx)/)?.[1] ?? "unknown";
      add("ICE candidate", `type=${kind} mid=${candidate.sdpMid ?? "—"}`);
    }
    return Reflect.apply(originalCandidate, this, [candidate]) as Promise<void>;
  } as typeof proto.addIceCandidate;

  add("Inspector", "Installed RTCPeerConnection metadata tracing");
}

export function getProtocolEvents(): ProtocolEvent[] {
  return typeof window === "undefined" ? [] : (window.__signalRoomTrace ?? []);
}
