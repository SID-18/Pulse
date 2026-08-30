package com.pulse.realtime;

import com.pulse.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSecurityChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketSecurityChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new AccessDeniedException("A valid access token is required.");
            }
            Claims claims = jwtService.parseToken(authorization.substring(7));
            String role = claims.get("role", String.class);
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                AuthorityUtils.createAuthorityList("ROLE_" + role)
            ));
        }
        return message;
    }
}
