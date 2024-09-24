package ok.backend.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {
    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    public StompExceptionHandler() {
        super();
    }

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage,
                                                              Throwable ex) {

        final Throwable exception = converterTrowException(ex);

        if (exception instanceof HttpClientErrorException.Unauthorized) {
            return handleUnauthorizedException(clientMessage, exception);
        }

        return super.handleClientMessageProcessingError(clientMessage, ex);

    }

    private Throwable converterTrowException(final Throwable exception) {
        if (exception instanceof MessageDeliveryException) {
            return exception.getCause();
        }
        return exception;
    }

    private Message<byte[]> handleUnauthorizedException(Message<byte[]> clientMessage,
                                                        Throwable ex) {

        return prepareErrorMessage(clientMessage, ex.getMessage(), HttpStatus.UNAUTHORIZED.name());

    }

    private Message<byte[]> prepareErrorMessage(final Message<byte[]> clientMessage,
                                                final String message, final String errorCode) {

        final StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        stompHeaderAccessor.setMessage(errorCode);
        stompHeaderAccessor.setLeaveMutable(true);

        setReceiptIdForClient(clientMessage, stompHeaderAccessor);

        return MessageBuilder.createMessage(
                message != null ? message.getBytes(StandardCharsets.UTF_8) : EMPTY_PAYLOAD,
                stompHeaderAccessor.getMessageHeaders()
        );
    }

    private void setReceiptIdForClient(final Message<byte[]> clientMessage,
                                       final StompHeaderAccessor stompHeaderAccessor) {

        if (Objects.isNull(clientMessage)) {
            return;
        }

        final StompHeaderAccessor clientHeaderAccessor = MessageHeaderAccessor.getAccessor(
                clientMessage, StompHeaderAccessor.class);

        final String receiptId =
                Objects.isNull(clientHeaderAccessor) ? null : clientHeaderAccessor.getReceipt();

        if (receiptId != null) {
            stompHeaderAccessor.setReceiptId(receiptId);
        }
    }

    //2
    @Override
    protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor,
                                             byte[] errorPayload, Throwable cause, StompHeaderAccessor clientHeaderAccessor) {

        return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());

//        return super.handleInternal(errorHeaderAccessor, errorPayload, cause, clientHeaderAccessor);
    }
}
