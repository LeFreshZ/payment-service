package com.innowise.paymentservice.config;

import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    Long userId = source.getClaim("userId");
    String role = source.getClaim("role");

    Collection<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

    return new JwtAuthenticationToken(source, authorities, userId.toString());
  }
}
