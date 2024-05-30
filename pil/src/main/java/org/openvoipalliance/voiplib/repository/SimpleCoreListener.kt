package org.openvoipalliance.voiplib.repository

import org.linphone.core.*

internal interface SimpleCoreListener : CoreListener {

    override fun onCallIdUpdated(core: Core, previousCallId: String, currentCallId: String) {}

    override fun onNetworkReachable(lc: Core, reachable: Boolean) {}

    override fun onReferReceived(lc: Core, referTo: String) {}

    override fun onQrcodeFound(lc: Core, result: String?) {}

    override fun onGlobalStateChanged(lc: Core, gstate: GlobalState, message: String) {}

    override fun onSubscriptionStateChanged(lc: Core, lev: Event, state: SubscriptionState) {}

    override fun onCallStatsUpdated(lc: Core, call: Call, stats: CallStats) {}

    override fun onFriendListRemoved(lc: Core, list: FriendList) {}

    override fun onChatRoomSubjectChanged(lc: Core, cr: ChatRoom) {}

    override fun onCallCreated(lc: Core, call: Call) {}

    override fun onMessageReceivedUnableDecrypt(lc: Core, room: ChatRoom, message: ChatMessage) {}

    override fun onMessageSent(lc: Core, room: ChatRoom, message: ChatMessage) {}

    override fun onNotifyPresenceReceivedForUriOrTel(
        lc: Core,
        lf: Friend,
        uriOrTel: String,
        presenceModel: PresenceModel,
    ) {}

    override fun onChatRoomStateChanged(lc: Core, cr: ChatRoom, state: ChatRoom.State) {}

    override fun onChatRoomEphemeralMessageDeleted(lc: Core, cr: ChatRoom) {}

    override fun onNotifyPresenceReceived(lc: Core, lf: Friend) {}

    override fun onChatRoomRead(lc: Core, room: ChatRoom) {}

    override fun onAuthenticationRequested(lc: Core, authInfo: AuthInfo, method: AuthMethod) {}

    override fun onCallLogUpdated(lc: Core, newcl: CallLog) {}

    override fun onIsComposingReceived(lc: Core, room: ChatRoom) {}

    override fun onEcCalibrationAudioUninit(lc: Core) {}

    override fun onCallEncryptionChanged(
        lc: Core,
        call: Call,
        on: Boolean,
        authenticationToken: String?,
    ) {}

    override fun onEcCalibrationResult(lc: Core, status: EcCalibratorStatus, delayMs: Int) {}

    override fun onLogCollectionUploadProgressIndication(lc: Core, offset: Int, total: Int) {}

    override fun onPublishStateChanged(lc: Core, lev: Event, state: PublishState) {}

    override fun onFriendListCreated(lc: Core, list: FriendList) {}

    override fun onConfiguringStatus(lc: Core, status: ConfiguringState, message: String?) {}

    override fun onLogCollectionUploadStateChanged(
        lc: Core,
        state: Core.LogCollectionUploadState,
        info: String,
    ) {}

    override fun onMessageReceived(lc: Core, room: ChatRoom, message: ChatMessage) {}

    override fun onBuddyInfoUpdated(lc: Core, lf: Friend) {}

    override fun onTransferStateChanged(lc: Core, transfered: Call, newCallState: Call.State) {}

    override fun onInfoReceived(lc: Core, call: Call, msg: InfoMessage) {}

    override fun onNewSubscriptionRequested(lc: Core, lf: Friend, url: String) {}

    override fun onDtmfReceived(lc: Core, call: Call, dtmf: Int) {}

    override fun onEcCalibrationAudioInit(lc: Core) {}

    override fun onVersionUpdateCheckResultReceived(
        lc: Core,
        result: VersionUpdateCheckResult,
        version: String,
        url: String?,
    ) {}

    override fun onRegistrationStateChanged(
        lc: Core,
        cfg: ProxyConfig,
        cstate: RegistrationState,
        message: String,
    ) {}

    override fun onCallStateChanged(
        lc: Core,
        linphoneCall: Call,
        state: Call.State,
        message: String,
    ) {}

    override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {}

    override fun onImeeUserRegistration(
        core: Core,
        status: Boolean,
        userId: String,
        info: String,
    ) {}

    override fun onFirstCallStarted(core: Core) {}

    override fun onConferenceStateChanged(
        core: Core,
        conference: Conference,
        state: Conference.State?,
    ) {}

    override fun onLastCallEnded(core: Core) {}

    override fun onAudioDevicesListUpdated(core: Core) {}

    override fun onAccountRegistrationStateChanged(
        core: Core,
        account: Account,
        state: RegistrationState?,
        message: String
    ) {}

    override fun onPushNotificationReceived(core: Core, payload: String?) {}

    override fun onMessagesReceived(
        core: Core,
        chatRoom: ChatRoom,
        messages: Array<out ChatMessage>
    ) {}

    override fun onCallGoclearAckSent(core: Core, call: Call) {}

    override fun onConferenceInfoReceived(core: Core, conferenceInfo: ConferenceInfo) {}

    override fun onPreviewDisplayErrorOccurred(p0: Core, p1: Int) {}

    override fun onNewMessageReaction(
        p0: Core,
        p1: ChatRoom,
        p2: ChatMessage,
        p3: ChatMessageReaction,
    ) {}

    override fun onReactionRemoved(p0: Core, p1: ChatRoom, p2: ChatMessage, p3: Address) {}

    override fun onChatRoomSessionStateChanged(
        p0: Core,
        p1: ChatRoom,
        p2: Call.State?,
        p3: String,
    ) {}

    override fun onCallSendMasterKeyChanged(p0: Core, p1: Call, p2: String?) {}

    override fun onCallReceiveMasterKeyChanged(p0: Core, p1: Call, p2: String?) {}

    override fun onNotifySent(p0: Core, p1: Event, p2: Content?) {}

    override fun onNotifyReceived(p0: Core, p1: Event, p2: String, p3: Content?) {}

    override fun onSubscribeReceived(p0: Core, p1: Event, p2: String, p3: Content?) {}

    override fun onPublishReceived(p0: Core, p1: Event, p2: String, p3: Content?) {}

    override fun onDefaultAccountChanged(p0: Core, p1: Account?) {}

    override fun onAccountAdded(p0: Core, p1: Account) {}

    override fun onAccountRemoved(p0: Core, p1: Account) {}

    override fun onNewAlertTriggered(p0: Core, p1: Alert) {}
}