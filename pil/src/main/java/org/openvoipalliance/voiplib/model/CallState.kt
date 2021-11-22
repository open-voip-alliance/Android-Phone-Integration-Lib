package org.openvoipalliance.voiplib.model

enum class CallState {
    Idle,
    IncomingReceived,
    OutgoingInit,
    OutgoingProgress,
    OutgoingRinging,
    OutgoingEarlyMedia,
    Connected,
    StreamsRunning,
    Pausing,
    Paused,
    Resuming,
    Referred,
    Error,
    CallEnd,
    PausedByRemote,
    CallUpdatedByRemote,
    CallIncomingEarlyMedia,
    CallUpdating,
    CallReleased,
    CallEarlyUpdatedByRemote,
    CallEarlyUpdating,
    Unknown
}