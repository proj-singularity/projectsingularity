package com.projectsingularity.backend.auth.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.projectsingularity.backend.auth.entities.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityUtil {
  private static final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

  public static User getAuthenticatedUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof User user) {
      return user;
    } else {
      log.error("User requested but not found in SecurityContextHolder");
      throw new RuntimeException("User requested but not found in SecurityContextHolder");
    }
  }
}
