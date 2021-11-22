package org.openvoipalliance.voiplib.model


enum class Reason(val value: Int) {
    NONE(0),
    NO_RESPONSE(1),
    BAD_CREDENTIALS(2),
    DECLINED(3),
    NOT_FOUND(4),
    NOT_ANSWERED(5),
    BUSY(6),
    MEDIA(7),
    IO_ERROR(8),
    DO_NOT_DISTURB(9),
    UNAUTHORISED(10),
    NOT_ACCEPTABLE(11),
    NO_MATCH(12),
    MOVED_PERMANENTLY(13),
    GONE(14),
    TEMPORARILY_UNAVAILABLE(15),
    ADDRESS_INCOMPLETE(16),
    NOT_IMPLEMENTED(17),
    BAD_GATEWAY(18),
    SERVER_TIMEOUT(19),
    UNKNOWN(20)
}